package docSharing.service;

import docSharing.entity.*;
import docSharing.repository.*;
import docSharing.requests.Method;
import docSharing.utils.ExceptionMessage;
import docSharing.utils.debounce.Debouncer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.util.*;

@Service
public class DocumentService implements ServiceInterface {
    static Map<Long, String> documentsContentLiveChanges = new HashMap<>(); // current content in cache
    static Map<Long, String> databaseDocumentsCurrentContent = new HashMap<>(); // current content in database
    //<docId,<userId, log>>
    static Map<Long, Map<Long, Log>> chainedLogs = new HashMap<>(); // logs history until storing to database
    static Map<Long, Set<User>> onlineUsersPerDoc = new HashMap<>();
    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    FolderRepository folderRepository;
    @Autowired
    UserDocumentRepository userDocumentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    LogRepository logRepository;

    Debouncer debouncer = new Debouncer<>(new SendLogsToDatabase(chainedLogs), 5000);

    @Scheduled(fixedDelay = 10000)
    public void updateDatabaseWithNewContent() {
        for (Map.Entry<Long, String> entry : documentsContentLiveChanges.entrySet()) {
            if (!entry.getValue().equals(databaseDocumentsCurrentContent.get(entry.getKey()))) {
                documentRepository.updateContent(entry.getValue(), entry.getKey());
                databaseDocumentsCurrentContent.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * This function called every time we get a new log,
     * checks if a new data that was written to document was written before
     * the logs that are online in chainedLogs map, if it ws before we will update the offsets accordingly.
     *
     * @param log - changes from
     */
    private void updateLogsOffset(Map<Long, Log> documentLogs, Log log) {
        documentLogs.replaceAll((userId, _log) -> {
            // create a copy of the log in case we need to modify it
            Log tempLog = Log.copy(_log);

            // make sure not to change the current user's log
            if (log.getUser().getId() != userId) {

                // if the offset is before other logs' offset, decrease its offset by the length of the log
                if (log.getAction().equals("delete") && log.getOffset() <= _log.getOffset()) {
                    tempLog.setOffset(_log.getOffset() - log.getData().length());
                }

                // if the offset is before other logs' offset, increase its offset by the length of the log
                else if (log.getAction().equals("insert") && log.getOffset() <= _log.getOffset()) {
                    tempLog.setOffset(_log.getOffset() + log.getData().length());
                }

                // if the offset is in the middle of the logs' offset, split it to two, commit the first one and store only the second part
                else if (log.getOffset() > _log.getOffset() && log.getOffset() < _log.getOffset() + _log.getData().length()) {
                    // cut the _log to half
                    Log firstPartOfLog = Log.copy(_log);
                    firstPartOfLog.setData(_log.getData().substring(0, log.getOffset()));
                    firstPartOfLog.setLastEditDate(_log.getCreationDate());
                    System.out.println("firstLog" + firstPartOfLog);
                    // store the first half in the database. for now just print it
                    firstPartOfLog.getUser().addLog(firstPartOfLog);
                    firstPartOfLog.getDocument().addLog(firstPartOfLog);
                    logRepository.save(firstPartOfLog);
                    //TODO: save firstPartOfLog to db

                    // keep the second half in the cache
                    // there's not a real need to store it in a different log, but for simplicity...
                    Log secondPartOfLog = Log.copy(_log);
                    secondPartOfLog.setOffset(log.getOffset() + 1);
                    secondPartOfLog.setData(_log.getData().substring(log.getOffset()));
                    secondPartOfLog.setLastEditDate(_log.getCreationDate());
                    System.out.println("secondLog" + secondPartOfLog);

                    // firstPartLog.sendtoDB!!!!
                    tempLog = secondPartOfLog;
                }

                // if the offset is after the log's data, skip it because it doesn't matter.
            }
            return tempLog;
        });
    }

    /**
     * the goal of this function is to present all live users that are using a specific document.
     * this function gets called when we want to add a new user to a document or delete the user from document,
     *
     * @param userId     - user's id in database.
     * @param documentId - document's id in database.
     * @param method     - ADD/ REMOVE
     * @return set of current users that viewing the document.
     */
    public Set<User> addUserToDocActiveUsers(Long userId, Long documentId, Method method) {
        onlineUsersPerDoc.putIfAbsent(documentId, new HashSet<>());
        if (!userRepository.findById(userId).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        }
        User user = userRepository.findById(userId).get();
        switch (method) {
            case ADD:
                onlineUsersPerDoc.get(documentId).add(user);
                break;
            case REMOVE:
                onlineUsersPerDoc.get(documentId).remove(user);
        }
        return onlineUsersPerDoc.get(documentId);
    }

    /**
     * main function that deals with new logs,
     * the goal is to make order in all the data that was entered to the document,
     * and to save / chain logs accordingly to who it was changed from.
     * send all data to inner functions to deal with new data and build the logs appropriately.
     *
     * @param log - log with new data.
     */
    public void updateContent(Log log) {

        debouncer.call(log, logRepository);

        if (!documentRepository.findById(log.getDocument().getId()).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString() + log.getDocument().getId());
        }
        if (!documentsContentLiveChanges.containsKey(log.getDocument().getId())) {
            Document doc = documentRepository.findById(log.getDocument().getId()).get();

            if (doc.getContent() == null) doc.setContent("");
            documentsContentLiveChanges.put(log.getDocument().getId(), doc.getContent());

        }
        // update document content string
        updateCurrentContentCache(log);

        Map<Long, Log> documentLogs = chainedLogs.get(log.getDocument().getId());

        if (documentLogs == null) {
            documentLogs = new HashMap<>();
            chainedLogs.put(log.getDocument().getId(), documentLogs);
        }
        // update logs
        chainLogs(documentLogs, log);
        updateLogsOffset(documentLogs, log);
    }

    /**
     * the goal of this function is to update the cached documentsContentLiveChanges map with new changes.
     * the new log is sent to inner functions called concatenateStrings/truncateString according to if it was insert/delete.
     *
     * @param log - log with new data.
     */
    private void updateCurrentContentCache(Log log) {
        switch (log.getAction()) {
            case "delete":
//                log.setData(String.valueOf(documentsContentLiveChanges.get(log.getDocumentId()).charAt(log.getOffset())));
                log.setData(String.valueOf(documentsContentLiveChanges.get(log.getDocument().getId()).charAt(log.getOffset())));
                log.setLastEditDate(log.getCreationDate());
                System.out.println("log" + log);

//                documentsContentLiveChanges.put(log.getDocumentId(), truncateString(documentsContentLiveChanges.get(log.getDocumentId()), log));
                documentsContentLiveChanges.put(log.getDocument().getId(), truncateString(documentsContentLiveChanges.get(log.getDocument().getId()), log));

                break;
            case "insert":
//                documentsContentLiveChanges.put(log.getDocumentId(), concatenateStrings(documentsContentLiveChanges.get(log.getDocumentId()), log));
                documentsContentLiveChanges.put(log.getDocument().getId(), concatenateStrings(documentsContentLiveChanges.get(log.getDocument().getId()), log));
                break;
        }
    }

    /**
     * this function goal is to connect 2 logs to 1 log according to the user that was written the new data
     * and the offset that the data was entered.
     * checking order is:
     * 1. if such a log doesn't exist in the cache, create a new entry for it in the map
     * 2. if the new log is not a sequel to current log, store the current one in the db and start a new one instead.
     * 3. if the current log was attempting to delete and how we want to insert,push the deleted one and create a new log
     * 4. if the new log is in the middle of the current log, it must be concatenated.
     * saves the concatenated logs to chainedLogs map.
     *
     * @param newLog - new data that needed to chain to old log.
     */
    private void chainLogs(Map<Long, Log> documentLogs, Log newLog) {


        // if such a log doesn't exist in the cache, create a new entry for it in the map
        if (!documentLogs.containsKey(newLog.getUser().getId())) {
            documentLogs.put(newLog.getUser().getId(), newLog);
            return;
        }

        Log currentLog = documentLogs.get(newLog.getUser().getId());
        currentLog.setLastEditDate(newLog.getCreationDate());

//        System.out.println("Is current log start point greater? " + (currentLog.getOffset() - 1 >= newLog.getOffset()));
//        System.out.println("Is current log start point greater? " + (currentLog.getOffset() + currentLog.getData().length() + 1 <= newLog.getOffset()));
        // if the new log is not a sequel to current log, store the current one in the db and start a new one instead.
        if ((currentLog.getOffset() - 1 >= newLog.getOffset() || currentLog.getOffset() + currentLog.getData().length() + 1 <= newLog.getOffset())) {
            currentLog.getUser().addLog(currentLog);
            currentLog.getDocument().addLog(currentLog);
            logRepository.save(currentLog);
            documentLogs.put(currentLog.getUser().getId(), newLog);
            return;
        }

        // if the new log is in the middle of the current log, it must be concatenated.
        else {
            if (currentLog.getAction().equals("insert") && newLog.getAction().equals("delete")) {
                currentLog.setData(truncateLogs(currentLog, newLog));
                // if the current log was attempting to delete and now we want to insert, push the delete and create a new log
            } else if (currentLog.getAction().equals("delete") && newLog.getAction().equals("insert")) {
                currentLog.getUser().addLog(currentLog);
                currentLog.getDocument().addLog(currentLog);
                logRepository.save(currentLog); //TODO: save currentLog to db
                documentLogs.put(currentLog.getUser().getId(), newLog);
                return;

            } else if (newLog.getAction().equals(currentLog.getAction())) {
                currentLog.setData(concatenateLogs(currentLog, newLog));
            }

            documentLogs.put(currentLog.getUser().getId(), currentLog);
        }
    }

    /**
     * @param id - document id.
     * @return entity of Document from database
     * @throws AccountNotFoundException - no document in database with given id.
     */
    public Document findById(Long id) throws AccountNotFoundException {
        if (!documentRepository.findById(id).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString());
        return documentRepository.findById(id).get();
    }

    /**
     * addToMap used when a new document was created and needed to add to the cached maps of logs of documents.
     *
     * @param id - of document
     */
    void addToMap(long id) {
        documentsContentLiveChanges.put(id, "");
        databaseDocumentsCurrentContent.put(id, "");
    }

    /**
     * @param id - document id
     * @return - Document entity
     * @throws AccountNotFoundException - Could not locate this document in the database.
     */
    public Document getDocById(Long id) throws AccountNotFoundException {
        if (!documentRepository.findById(id).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString());
        return documentRepository.findById(id).get();
    }

    /**
     * this function gets called when we want to show to te client all the documents that in a specific folder.
     *
     * @param parentFolderId - parent folder
     * @param userId         - current user
     * @return list with all the document entities.
     */
    public List<Document> get(Long parentFolderId, Long userId) throws AccountNotFoundException {
        if (!folderRepository.findById(parentFolderId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_FOLDER_IN_DATABASE.toString());
        if (!userRepository.findById(userId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        User user = userRepository.findById(userId).get();
        Folder parentFolder = folderRepository.findById(parentFolderId).get();
        return documentRepository.findAllByUserIdAndParentFolderId(parentFolder, user);
    }

    /**
     * function get an item of kind document and uses the logics to create and save a new folder to database.
     * first check if we have a folder to create the document into.
     * then create the document and add to the folder the new document in the database,
     * same as the user is needed to be assigned to the new document.
     * set Permission of the creator as an MODERATOR.
     *
     * @param generalItem - document.
     * @return id of document.
     */
    public Long create(GeneralItem generalItem) {
        if (generalItem.getParentFolder() != null) {
            Optional<Folder> folder = folderRepository.findById(generalItem.getParentFolder().getId());
            if (!folder.isPresent())
                throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString() + generalItem.getParentFolder().getId());
        }
        Document savedDoc = documentRepository.save((Document) generalItem);
        addToMap(savedDoc.getId());
        if (savedDoc.getParentFolder() != null) {
            savedDoc.getParentFolder().addDocument(savedDoc);
        }
        savedDoc.getUser().addDocument(savedDoc);
        UserDocument userDocument = new UserDocument();
        userDocument.setId(new UserDocumentPk());
        userDocument.setDocument(savedDoc);
        userDocument.setUser(savedDoc.getUser());
        userDocument.setPermission(Permission.ADMIN);
        userDocumentRepository.save(userDocument);
        return savedDoc.getId();
    }

    /**
     * this function goal is to change name of a document to a new one.
     *
     * @param docId - document id in database
     * @param name  - new document name to change to.
     * @return - rows that was affected in database (1).
     */
    public int rename(Long docId, String name) {
        if (documentRepository.findById(docId).isPresent()) {
            return documentRepository.updateName(name, docId);
        }
        throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
    }

    /**
     * this function goal is to show the live content of a document to the client.
     *
     * @param documentId - document id
     * @return content in documentsContentLiveChanges
     */
    public String getContent(Long documentId) {
        String content = documentsContentLiveChanges.get(documentId);
        if (content == null || content.length() == 0) {
            String databaseContent = documentRepository.getContentFromDocument(documentId);
            documentsContentLiveChanges.put(documentId, databaseContent);
            databaseDocumentsCurrentContent.put(documentId, databaseContent);
        }
        return documentsContentLiveChanges.get(documentId);
    }

    /**
     * this function gets called from updateCurrentContentCache when the document content was changed.
     * the goal is to put the log in the correct offset we have and update the string accordingly.
     *
     * @param text - document content
     * @param log  - log with new data
     * @return updated content
     */
    private String concatenateStrings(String text, Log log) {
        String beforeCut = text.substring(0, log.getOffset());
        String afterCut = text.substring(log.getOffset());
        return beforeCut + log.getData() + afterCut;
    }

    /**
     * this function gets called from chainLogs when the logs are needed to concatenate.
     * change the newLog offset to put him according to the current log.
     * it comes from that idea that we want to make the currentLog offset as our absolute zero.
     *
     * @param currentLog - log that is in the cached map of logs.
     * @param newLog     - log with changes from the client.
     * @return - updated content that was concatenated from the 2 logs we have.
     */
    private String concatenateLogs(Log currentLog, Log newLog) {
        int diff = newLog.getOffset() - currentLog.getOffset() < 0 ? 0 : newLog.getOffset() - currentLog.getOffset();
        newLog.setOffset(diff);
        if (currentLog.getData() == null) currentLog.setData(""); // CONSULT: shouldn't happen, so why did I write it?
        return concatenateStrings(currentLog.getData(), newLog);
    }

    /**
     * this function gets called from updateCurrentContentCache when the document content was changed.
     * the goal is to delete the data that is in the log and to apply it on the correct offset in the content at the doc.
     *
     * @param text - document content.
     * @param log  - log with new data.
     * @return updated content.
     */
    private String truncateString(String text, Log log) {
        String beforeCut = text.substring(0, log.getOffset());
        String afterCut = text.substring(log.getOffset() + log.getData().length());
        return beforeCut.concat(afterCut);
    }

    /**
     * this function gets called from chainLogs when the logs are needed to truncate.
     * change the newLog offset to put him according to the current log.
     * it comes from that idea that we want to make the currentLog offset as our absolute zero.
     *
     * @param currentLog - log that is in the cached map of logs.
     * @param newLog     - log with changes from the client.
     * @return - updated content that was truncated from the 2 logs we have.
     */
    private String truncateLogs(Log currentLog, Log newLog) {
        newLog.setOffset(newLog.getOffset() - (currentLog.getOffset()));
        if (currentLog.getData() == null) currentLog.setData("");
        return truncateString(currentLog.getData(), newLog);
    }

    /**
     * relocate is to change the document's location.
     *
     * @param newParentFolder - the folder that document is located.
     * @param id              - document id.
     * @return rows affected in mysql.
     */
    public int relocate(Folder newParentFolder, Long id) {
        if (newParentFolder != null && !folderRepository.findById(newParentFolder.getId()).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
        }
        if (!documentRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
        }
        Document doc = documentRepository.findById(id).get();
        Folder oldParentFolder = doc.getParentFolder();
        doc.setParentFolder(newParentFolder);
        oldParentFolder.removeDocument(doc);
        if (newParentFolder != null) {
            newParentFolder.addDocument(doc);
        }
        return documentRepository.updateParentFolderId(newParentFolder, id);
    }

    /**
     * delete file by getting the document id,
     * also remove from the maps of content we have on service.
     *
     * @param docId - gets document id .
     */
    public void delete(Long docId) {
        databaseDocumentsCurrentContent.remove(docId);
        documentsContentLiveChanges.remove(docId);
        if (!documentRepository.findById(docId).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
        }
        userDocumentRepository.deleteDocument(documentRepository.findById(docId).get());
        documentRepository.deleteById(docId);
    }

    public List<Document> getAllWhereParentFolderIsNull(Long userId) throws AccountNotFoundException {
        if (!userRepository.findById(userId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        User user = userRepository.findById(userId).get();
        return documentRepository.findAllByParentFolderIsNull(user);
    }

    public List<UserDocument> getAllUsersInDocument(Long documentId) throws AccountNotFoundException {
        if (!documentRepository.findById(documentId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        Document document = documentRepository.findById(documentId).get();
        return userDocumentRepository.findAllUsersInDocument(document);
    }

    public Permission getUserPermissionInDocument(Long userId, Long documentId) throws AccountNotFoundException {
        if (!documentRepository.findById(documentId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        if (!userRepository.findById(userId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        User user = userRepository.findById(userId).get();
        Document document = documentRepository.findById(documentId).get();
        Optional<UserDocument> userDocument = userDocumentRepository.find(document, user);
        if (!userDocument.isPresent()) {
            return Permission.UNAUTORIZED;
        }
        return userDocument.get().getPermission();
    }

    public UserDocument saveUserInDocument(UserDocument userDocument) {
        return userDocumentRepository.save(userDocument);
    }
}
