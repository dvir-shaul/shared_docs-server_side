package docSharing.service;

import docSharing.entity.*;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
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

    Debouncer debouncer = new Debouncer<>(new SendLogsToDatabase(chainedLogs), 5000);

    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    FolderRepository folderRepository;


    private void updateLogsOffset(Log log) {

        chainedLogs.replaceAll((userId, _log) -> {
            // create a copy of the log in case we need to modify it
            Log tempLog = Log.copy(_log);

            // make sure not to change the current user's log
            if (log.getUserId() != userId) {

                // if the offset is before other logs' offset, increase its offset by the length of the log
                if (log.getOffset() <= _log.getOffset()) {
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

    public void updateContent(Log log) {

        debouncer.call(log.getUserId());

        if (!documentRepository.findById(log.getDocumentId()).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString() + log.getDocumentId());
        }

        if (!documentsContentLiveChanges.containsKey(log.getDocumentId())) {
            // FIXME: we need to check if there's a doc in the database
            // FIXME: if not, we need to create a new one or to throw an exception that this doc doesn't exist.
            Document doc = documentRepository.findById(log.getDocumentId()).get();
            if(doc.getContent() == null) doc.setContent("");
            documentsContentLiveChanges.put(log.getDocumentId(), doc.getContent());
        }

        switch (log.getAction()) {
            case "delete":
                documentsContentLiveChanges.put(log.getDocumentId(), truncateString(documentsContentLiveChanges.get(log.getDocumentId()), log));
            case "insert":
                documentsContentLiveChanges.put(log.getDocumentId(), concatenateStrings(documentsContentLiveChanges.get(log.getDocumentId()), log));
            default:
                chainLogs(chainedLogs.get(log.getUserId()), log);
                updateLogsOffset(log);

        }
    }

    private void chainLogs(Log currentLog, Log newLog) {

        if (!chainedLogs.containsKey(newLog.getUserId())) {
            chainedLogs.put(newLog.getUserId(), newLog);
            return;
        }
//        if(currentLog.getDocumentId() != newLog.getDocumentId()) return; // send current log to db and make map null

        if ((currentLog.getOffset() - 1 >= newLog.getOffset() && currentLog.getOffset() + currentLog.getData().length() + 1 <= newLog.getOffset()))
            // send the old log to the database, reset its place in the map and place the new log inside it.
            chainedLogs.put(currentLog.getUserId(), newLog);
//        return;
        else {
            // change to concatenateLogs
            currentLog.setData(concatenateLogs(currentLog, newLog));
            chainedLogs.put(currentLog.getUserId(), currentLog);
        }
    }

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
        return documentsContentLiveChanges.get(id);
    }

    private String concatenateStrings(String text, Log log) {
        String beforeCut = text.substring(0, log.getOffset());
        String afterCut = text.substring(log.getOffset());
        return beforeCut + log.getData() + afterCut;
    }

    private String concatenateLogs(Log currentLog, Log newLog) {
        int distanceBetweenLogsOffset = newLog.getOffset() - (currentLog.getOffset());
        newLog.setOffset(distanceBetweenLogsOffset);
        if (currentLog.getData() == null) currentLog.setData("");
        return concatenateStrings(currentLog.getData(), newLog);
    }

    private String truncateString(String text, Log log) {
        String beforeCut = text.substring(0, log.getOffset());
        String afterCut = text.substring(log.getOffset() + Integer.valueOf(log.getData()));
        return beforeCut.concat(afterCut);
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
        documentRepository.deleteById(docId);
    }
}
