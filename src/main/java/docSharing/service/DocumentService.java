package docSharing.service;

import docSharing.entity.*;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserDocumentRepository;
import docSharing.repository.UserRepository;
import docSharing.utils.ExceptionMessage;
import docSharing.utils.debounce.Debouncer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DocumentService implements ServiceInterface {

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

//    @Scheduled(fixedDelay = 10000)
//    public void updateDatabaseWithNewContent() {
//        for (Map.Entry<Long, String> entry : documentsContentLiveChanges.entrySet()) {
//            if (!entry.getValue().equals(databaseDocumentsCurrentContent.get(entry.getKey()))) {
//                documentRepository.updateContent(entry.getValue(), entry.getKey());
//                databaseDocumentsCurrentContent.put(entry.getKey(), entry.getValue());
//            }
//        }
//    }

    private void updateLogsOffset(Log log) {

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

    public Set<User> addUserToDocActiveUsers(Long userId, Long documentId){
        onlineUsersPerDoc.putIfAbsent(documentId, new HashSet<>());
        // FIXME: check if this user id even exists in the db
        User user = userRepository.findById(userId).get();
        onlineUsersPerDoc.get(documentId).add(user);
        return onlineUsersPerDoc.get(documentId);
    }

    public void updateContent(Log log) {

        debouncer.call(log.getUserId());

        if (!documentRepository.findById(log.getDocumentId()).isPresent()) {
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

    private void updateCurrentContentCache(Log log) {
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

    private void chainLogs(Log currentLog, Log newLog) {

        // if such a log doesn't exist in the cache, create a new entry for it in the map
        if (!chainedLogs.containsKey(newLog.getUserId())) {
            chainedLogs.put(newLog.getUserId(), newLog);
            return;
        }

        // if the new log is not a sequel to current log, store the current one in the db and start a new one instead.
        if ((currentLog.getOffset() - 1 >= newLog.getOffset() && currentLog.getOffset() + currentLog.getData().length() + 1 <= newLog.getOffset()))
            chainedLogs.put(currentLog.getUserId(), newLog);

        // if the current log was attempting to delete and how we want to insert, push the delete and create a new log
        if(currentLog.getAction().equals("delete") && newLog.getAction().equals("insert")){
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


    public Optional<Document> findById(Long id) {
        return documentRepository.findById(id);
    }

    /**
     * addToMap used when create a new document
     *
     * @param id - of document
     */
    void addToMap(long id) {
        documentsContentLiveChanges.put(id, "");
        databaseDocumentsCurrentContent.put(id, "");
    }

    public Document getDocById(Long id) {
        // FIXME: it is optional so we need to check if it exists
        return documentRepository.findById(id).get();
    }

    public List<Document> get(Long folderId, Long userId) {
        return documentRepository.findAllByUserIdAndParentFolderId(folderId, userId);
    }

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
        UserDocument userDocument=new UserDocument();
        userDocument.setId(new UserDocumentPk());
        userDocument.setDocument(savedDoc);
        userDocument.setUser(savedDoc.getUser());
        userDocument.setPermission(Permission.MODERATOR);
        userDocumentRepository.save(userDocument);
        System.out.println("A new doc has been added with an id of " + savedDoc.getId());
        return savedDoc.getId();
    }

    public int rename(Long id, String name) {
        if (documentRepository.findById(id).isPresent()) {
            return documentRepository.updateName(name, id);
        }
        throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
    }

    public String getContent(Long id) {
        String content = documentsContentLiveChanges.get(id);
        if(content == null) documentsContentLiveChanges.put(id, "");
        return documentsContentLiveChanges.get(id);
    }

    private String concatenateStrings(String text, Log log) {
        String beforeCut = text.substring(0, log.getOffset());
        String afterCut = text.substring(log.getOffset());
        return beforeCut + log.getData() + afterCut;
    }

    private String concatenateLogs(Log currentLog, Log newLog) {
        newLog.setOffset(newLog.getOffset() - (currentLog.getOffset()));
        if (currentLog.getData() == null) currentLog.setData(""); // CONSULT: shouldn't happen, so why did I write it?
        return concatenateStrings(currentLog.getData(), newLog);
    }

    private String truncateString(String text, Log log) {
        String beforeCut = text.substring(0, log.getOffset());
        String afterCut = text.substring(log.getOffset() + log.getData().length());
        return beforeCut.concat(afterCut);
    }

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
        userDocumentRepository.deleteDocument(documentRepository.findById(docId).get());
        documentRepository.deleteById(docId);
    }
    public Set<User> getOnlineUsers(Long documentId){
        return onlineUsersPerDoc.get(documentId);
    }
}
