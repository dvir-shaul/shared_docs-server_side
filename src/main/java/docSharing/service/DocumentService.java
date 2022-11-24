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

import javax.print.Doc;
import java.util.*;

@Service
public class DocumentService implements ServiceInterface {

    static Map<Long, String> documentsContentChanges = new HashMap<>(); // current content in cache
    static Map<Long, String> databaseDocumentsContent = new HashMap<>(); // current content in database
    static Map<Long, Log> changeLogs = new HashMap<>(); // logs history until storing to database
    //       userId, changesList

    Debouncer debouncer = new Debouncer<>(new SendLogsToDatabase(changeLogs), 10000);

    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    FolderRepository folderRepository;

    private void insertLogToLog(Log currentLog, Log newLog) {
//        if(currentLog.getDocumentId() != newLog.getDocumentId()) return; // send current log to db and make map null
//        if (!(currentLog.getOffset() - 1 >= newLog.getOffset() && currentLog.getOffset() + currentLog.getData().length() + 1 <= newLog.getOffset()))
//            return;
//        System.out.println(currentLog.getOffset() + " " + newLog.getOffset() + " " + (currentLog.getOffset() - 1 >= newLog.getOffset()));
//        System.out.println(currentLog.getOffset() + (currentLog.getData().length() + 1) + " " + newLog.getOffset() + " " + (currentLog.getOffset() + currentLog.getData().length() + 1 <= newLog.getOffset()));

        String currentText = currentLog.getData();
        String beforeCut = currentText.substring(0, newLog.getOffset());
        String afterCut = currentText.substring(newLog.getOffset());
        String content = beforeCut + newLog.getData() + afterCut;
        currentLog.setData(content);

        changeLogs.put(currentLog.getUserId(), currentLog);
    }

    public void updateContent(Log log) {
//        addToMap(log.getDocumentId());

//        if (!changeLogs.containsKey(log.getUserId())) changeLogs.put(log.getUserId(), log);
//        else {
//            insertLogToLog(changeLogs.get(log.getUserId()), log);
//        }

//        debouncer.call(log.getUserId());

        if (!documentRepository.findById(log.getDocumentId()).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
        }
        if (!documentsContentChanges.containsKey(log.getDocumentId())) {
            documentsContentChanges.put(log.getDocumentId(), documentRepository.findById(log.getDocumentId()).get().getContent());
        }

        switch (log.getAction()) {
            case "delete":
                deleteText(log);
                break;
            case "insert":
                insertText(log);
                break;
        }
    }

    @Autowired
    UserRepository userRepository;

    @Scheduled(fixedDelay = 10000)
    public void updateDatabaseWithNewContent() {
        for (Map.Entry<Long, String> entry : documentsContentChanges.entrySet()) {
            if (!entry.getValue().equals(databaseDocumentsContent.get(entry.getKey()))) {
                documentRepository.updateContent(entry.getValue(), entry.getKey());
                databaseDocumentsContent.put(entry.getKey(), entry.getValue());
            }
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
        documentsContentChanges.put(id, "");
        databaseDocumentsContent.put(id, "");
    }

    public Document getDocById(Long id) {
        // FIXME: it is optional so we need to check if it exists
        return documentRepository.findById(id).get();
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
        return savedDoc.getId();
    }

    public int rename(Long id, String name) {
        if (documentRepository.findById(id).isPresent()) {
            return documentRepository.updateName(name, id);
        }
        throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
    }

    public String getContent(Long id) {
        return documentsContentChanges.get(id);
    }


    private void insertText(Log log) {
        Long id = log.getDocumentId();

        String temp = documentsContentChanges.get(id);
        String beforeCut = temp.substring(0, log.getOffset());
        String afterCut = temp.substring(log.getOffset());
        String content = beforeCut + log.getData() + afterCut;

        documentsContentChanges.put(id, content);
    }


    private void deleteText(Log log) {
        Long id = log.getDocumentId();

        String temp = documentsContentChanges.get(id);
        String beforeCut = temp.substring(0, log.getOffset());
        String afterCut = temp.substring(log.getOffset() + Integer.valueOf(log.getData()));
        String content = beforeCut.concat(afterCut);

        documentsContentChanges.put(id, content);
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
        databaseDocumentsContent.remove(docId);
        documentsContentChanges.remove(docId);
        documentRepository.deleteById(docId);
    }
}
