package docSharing.service;

import docSharing.entity.*;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserDocumentRepository;
import docSharing.repository.UserRepository;
import docSharing.requests.Method;
import docSharing.utils.ExceptionMessage;
import docSharing.utils.debounce.Debouncer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.util.*;

@Service
public class DocumentService implements ServiceInterface {
    private static Logger logger = LogManager.getLogger(DocumentService.class.getName());

    static Map<Long, String> documentsContentLiveChanges = new HashMap<>(); // current content in cache
    static Map<Long, String> databaseDocumentsCurrentContent = new HashMap<>(); // current content in database
    static Map<Long, Log> chainedLogs = new HashMap<>(); // logs history until storing to database
    //       userId, changesList
    static Map<Long, Set<User>> onlineUsersPerDoc = new HashMap<>();

    Debouncer debouncer = new Debouncer<>(new SendLogsToDatabase(chainedLogs), 5000);

    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    FolderRepository folderRepository;
    @Autowired
    UserDocumentRepository userDocumentRepository;
    @Autowired
    UserRepository userRepository;

    /**
     * This function called every time we get a new log,
     * checks if a new data that was written to document was written before
     * the logs that are online in chainedLogs map, if it ws before we will update the offsets accordingly.
     * @param log - changes from
     */
    private void updateLogsOffset(Log log) {
        logger.info("in DocumentService -> updateLogsOffset");

        chainedLogs.replaceAll((userId, _log) -> {
            // create a copy of the log in case we need to modify it
            Log tempLog = Log.copy(_log);

            // make sure not to change the current user's log
            if (log.getUserId() != userId) {

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
                    // store the first half in the database. for now just print it
                    System.out.println(firstPartOfLog);

                    // keep the second half in the cache
                    // there's not a real need to store it in a different log, but for simplicity...
                    Log secondPartOfLog = Log.copy(_log);
                    secondPartOfLog.setOffset(log.getOffset() + 1);
                    secondPartOfLog.setData(_log.getData().substring(log.getOffset()));

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
     * @param userId - user's id in database.
     * @param documentId - document's id in database.
     * @param method - ADD/ REMOVE
     * @return set of current users that viewing the document.
     */
    public Set<User> addUserToDocActiveUsers(Long userId, Long documentId, Method method) {
        logger.info("in DocumentService -> addUserToDocActiveUsers");

        onlineUsersPerDoc.putIfAbsent(documentId, new HashSet<>());
        if(! userRepository.findById(userId).isPresent()) {
            logger.error("in DocumentService -> addUserToDocActiveUsers -> "+ExceptionMessage.NO_USER_IN_DATABASE.toString());
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
     * @param log - log with new data.
     */
    public void updateContent(Log log) {
        logger.info("in DocumentService -> updateContent");


        debouncer.call(log.getUserId());

        if (!documentRepository.findById(log.getDocumentId()).isPresent()) {
            logger.error("in DocumentService -> addUserToDocActiveUsers -> "+ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS + log.getDocumentId());
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString() + log.getDocumentId());
        }
        if (!documentsContentLiveChanges.containsKey(log.getDocumentId())) {
            Document doc = documentRepository.findById(log.getDocumentId()).get();
            if (doc.getContent() == null) doc.setContent("");
            documentsContentLiveChanges.put(log.getDocumentId(), doc.getContent());
        }

        updateCurrentContentCache(log);
        chainLogs(chainedLogs.get(log.getUserId()), log);
        updateLogsOffset(log);
    }

    /**
     * the goal of this function is to update the cached documentsContentLiveChanges map with new changes.
     * the new log is sent to inner functions called concatenateStrings/truncateString according to if it was insert/delete.
     * @param log - log with new data.
     */
    private void updateCurrentContentCache(Log log) {
        logger.info("in DocumentService -> updateCurrentContentCache");
        logger.debug(log.getAction());
        switch (log.getAction()) {
            case "delete":
                log.setData(String.valueOf(documentsContentLiveChanges.get(log.getDocumentId()).charAt(log.getOffset())));
                documentsContentLiveChanges.put(log.getDocumentId(), truncateString(documentsContentLiveChanges.get(log.getDocumentId()), log));
                break;
            case "insert":
                documentsContentLiveChanges.put(log.getDocumentId(), concatenateStrings(documentsContentLiveChanges.get(log.getDocumentId()), log));
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
     * @param currentLog - the log that is chainedLogs maps.
     * @param newLog - new data that needed to chain to old log.
     */
    private void chainLogs(Log currentLog, Log newLog) {
        logger.info("in DocumentService -> chainLogs");

        // if such a log doesn't exist in the cache, create a new entry for it in the map
        if (!chainedLogs.containsKey(newLog.getUserId())) {
            chainedLogs.put(newLog.getUserId(), newLog);
            return;
        }

        // if the new log is not a sequel to current log, store the current one in the db and start a new one instead.
        if ((currentLog.getOffset() - 1 >= newLog.getOffset() && currentLog.getOffset() + currentLog.getData().length() + 1 <= newLog.getOffset()))
            chainedLogs.put(currentLog.getUserId(), newLog);

        // if the current log was attempting to delete and how we want to insert, push the delete and create a new log
        if (currentLog.getAction().equals("delete") && newLog.getAction().equals("insert")) {
            chainedLogs.put(currentLog.getUserId(), newLog);
        }

        // if the new log is in the middle of the current log, it must be concatenated.
        else {
            if (newLog.getAction().equals("delete")) {
                currentLog.setData(truncateLogs(currentLog, newLog));
            } else if (newLog.getAction().equals("insert")) {
                currentLog.setData(concatenateLogs(currentLog, newLog));
            }
            // change to concatenateLogs
            chainedLogs.put(currentLog.getUserId(), currentLog);
        }
    }

    /**
     * @param id - document id.
     * @return entity of Document from database
     * @throws AccountNotFoundException - no document in database with given id.
     */
    public Document findById(Long id) throws AccountNotFoundException {
        logger.info("in DocumentService -> findById");

        if(! documentRepository.findById(id).isPresent()) {
            logger.error("in DocumentService -> findById -> "+ExceptionMessage.NO_DOCUMENT_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString());
        }
        return documentRepository.findById(id).get();
    }

    /**
     * addToMap used when a new document was created and needed to add to the cached maps of logs of documents.
     * @param id - of document
     */
    void addToMap(long id) {
        logger.info("in DocumentService -> addToMap");

        documentsContentLiveChanges.put(id, "");
        databaseDocumentsCurrentContent.put(id, "");
    }

    /**
     * @param id - document id
     * @return - Document entity
     * @throws AccountNotFoundException - Could not locate this document in the database.
     */
    public Document getDocById(Long id) throws AccountNotFoundException {
        logger.info("in DocumentService -> getDocById");

        if(! documentRepository.findById(id).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString());
        return documentRepository.findById(id).get();
    }

    /**
     * this function gets called when we want to show to te client all the documents that in a specific folder.
     * @param parentFolderId - parent folder
     * @param userId - current user
     * @return list with all the document entities.
     */
    public List<Document> get(Long parentFolderId, Long userId) throws AccountNotFoundException {
        logger.info("in DocumentService -> get");

        if(! folderRepository.findById(parentFolderId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_FOLDER_IN_DATABASE.toString());
        if(! userRepository.findById(userId).isPresent())
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
     * @param generalItem - document.
     * @return id of document.
     */
    public Long create(GeneralItem generalItem) {
        logger.info("in DocumentService -> create");

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
        System.out.println("++++++++++++++++++++++++++++++++++++");
        System.out.println(savedDoc.getName());
        savedDoc.getUser().addDocument(savedDoc);
        UserDocument userDocument = new UserDocument();
        userDocument.setId(new UserDocumentPk());
        userDocument.setDocument(savedDoc);
        userDocument.setUser(savedDoc.getUser());
        userDocument.setPermission(Permission.ADMIN);
        userDocumentRepository.save(userDocument);
        System.out.println("A new doc has been added with an id of " + savedDoc.getId());
        return savedDoc.getId();
    }

    /**
     * this function goal is to change name of a document to a new one.
     * @param docId - document id in database
     * @param name - new document name to change to.
     * @return - rows that was affected in database (1).
     */
    public int rename(Long docId, String name) {
        logger.info("in DocumentService -> rename");

        if (documentRepository.findById(docId).isPresent()) {
            return documentRepository.updateName(name, docId);
        }
        throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
    }
    /**
     * this function goal is to show the live content of a document to the client.
     * @param documentId - document id
     * @return content in documentsContentLiveChanges
     */
    public String getContent(Long documentId) {
        logger.info("in DocumentService -> getContent");

        String content = documentsContentLiveChanges.get(documentId);
        if (content == null) documentsContentLiveChanges.put(documentId, "");
        return documentsContentLiveChanges.get(documentId);
    }

    /**
     * this function gets called from updateCurrentContentCache when the document content was changed.
     * the goal is to put the log in the correct offset we have and update the string accordingly.
     * @param text - document content
     * @param log - log with new data
     * @return updated content
     */
    private String concatenateStrings(String text, Log log) {
        logger.info("in DocumentService -> concatenateStrings");

        String beforeCut = text.substring(0, log.getOffset());
        String afterCut = text.substring(log.getOffset());
        return beforeCut + log.getData() + afterCut;
    }

    /**
     * this function gets called from chainLogs when the logs are needed to concatenate.
     * change the newLog offset to put him according to the current log.
     * it comes from that idea that we want to make the currentLog offset as our absolute zero.
     * @param currentLog - log that is in the cached map of logs.
     * @param newLog - log with changes from the client.
     * @return - updated content that was concatenated from the 2 logs we have.
     */
    private String concatenateLogs(Log currentLog, Log newLog) {
        logger.info("in DocumentService -> concatenateLogs");

        newLog.setOffset(newLog.getOffset() - (currentLog.getOffset()));
        if (currentLog.getData() == null) currentLog.setData(""); // CONSULT: shouldn't happen, so why did I write it?
        return concatenateStrings(currentLog.getData(), newLog);
    }

    /**
     * this function gets called from updateCurrentContentCache when the document content was changed.
     * the goal is to delete the data that is in the log and to apply it on the correct offset in the content at the doc.
     * @param text - document content.
     * @param log - log with new data.
     * @return updated content.
     */
    private String truncateString(String text, Log log) {
        logger.info("in DocumentService -> truncateString");

        String beforeCut = text.substring(0, log.getOffset());
        String afterCut = text.substring(log.getOffset() + log.getData().length());
        return beforeCut.concat(afterCut);
    }
    /**
     * this function gets called from chainLogs when the logs are needed to truncate.
     * change the newLog offset to put him according to the current log.
     * it comes from that idea that we want to make the currentLog offset as our absolute zero.
     * @param currentLog - log that is in the cached map of logs.
     * @param newLog - log with changes from the client.
     * @return - updated content that was truncated from the 2 logs we have.
     */
    private String truncateLogs(Log currentLog, Log newLog) {
        logger.info("in DocumentService -> truncateLogs");

        newLog.setOffset(newLog.getOffset() - (currentLog.getOffset()));
        if (currentLog.getData() == null) currentLog.setData("");
        return truncateString(currentLog.getData(), newLog);
    }

    /**
     * relocate is to change the document's location.
     * @param newParentFolder - the folder that document is located.
     * @param id              - document id.
     * @return rows affected in mysql.
     */
    public int relocate(Folder newParentFolder, Long id) {
        logger.info("in DocumentService -> relocate");

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
     * @param docId - gets document id .
     */
    public void delete(Long docId) {
        logger.info("in DocumentService -> delete");

        databaseDocumentsCurrentContent.remove(docId);
        documentsContentLiveChanges.remove(docId);
        if(! documentRepository.findById(docId).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
        }
        userDocumentRepository.deleteDocument(documentRepository.findById(docId).get());
        documentRepository.deleteById(docId);
    }

    public List<Document> getAllWhereParentFolderIsNull(Long userId) throws AccountNotFoundException {
        logger.info("in DocumentService -> getAllWhereParentFolderIsNull");

        if(! userRepository.findById(userId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        User user = userRepository.findById(userId).get();
        return documentRepository.findAllByParentFolderIsNull(user);
    }

    public List<UserDocument> getAllUsersInDocument(Long documentId) throws AccountNotFoundException {
        logger.info("in DocumentService -> getAllUsersInDocument");

        if(! documentRepository.findById(documentId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        Document document = documentRepository.findById(documentId).get();
        return userDocumentRepository.findAllUsersInDocument(document);
    }

    public Permission getUserPermissionInDocument(Long userId, Long documentId) throws AccountNotFoundException {
        logger.info("in DocumentService -> getUserPermissionInDocument");

        if(! documentRepository.findById(documentId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        if(! userRepository.findById(userId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        User user=userRepository.findById(userId).get();
        Document document=documentRepository.findById(documentId).get();
        Optional<UserDocument> userDocument=userDocumentRepository.find(document, user);
        if(!userDocument.isPresent()){
            return Permission.UNAUTORIZED;
        }
        return userDocument.get().getPermission();
    }
    public UserDocument saveUserInDocument(UserDocument userDocument){
        logger.info("in DocumentService -> saveUserInDocument");

        System.out.println("in saveUserInDocument");
        System.out.println(userDocument);
        return userDocumentRepository.save(userDocument);
    }
}
