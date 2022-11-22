package docSharing.service;

import docSharing.entity.Document;
import docSharing.entity.GeneralItem;
import docSharing.entity.Folder;
import docSharing.entity.Log;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.utils.ExceptionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;

@Service
public class DocumentService implements ServiceInterface {

    static Map<Long, String> documentsContentChanges = new HashMap<>();
    static Map<Long, String> databaseDocumentsContent = new HashMap<>();


    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    FolderRepository folderRepository;


    @Scheduled(fixedDelay = 10000)
    public void updateDatabaseWithNewContent(){
        for (Map.Entry<Long,String> entry : documentsContentChanges.entrySet()) {
            if(! entry.getValue().equals(databaseDocumentsContent.get(entry.getKey()))){
                documentRepository.updateContent(entry.getValue(),entry.getKey());
                databaseDocumentsContent.put(entry.getKey(),entry.getValue());
            }
        }
    }

    /**
     * addToMap used when create a new document
     * @param id - of document
     */
    void addToMap(long id){
        documentsContentChanges.put(id,"");
        databaseDocumentsContent.put(id,"");
    }

    public Document getDocById(Long id) {
        return documentRepository.findById(id).get();
    }

    public Long create(GeneralItem generalItem) {
        if (generalItem.getParentFolderId() != null) {
            Optional<Folder> folder = folderRepository.findById(generalItem.getParentFolderId());
            if (!folder.isPresent())
                throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString() + generalItem.getParentFolderId());
        }
        addToMap(generalItem.getId());
        return documentRepository.save((Document) generalItem).getId();
    }

    /**
     * rename function gets an id of document and new name to change the document's name.
     *
     * @param id   - document id.
     * @param name - new name of the document.
     * @return rows affected in mysql.
     */
    public int rename(Long id, String name) {
        if (documentRepository.findById(id).isPresent()) {
            return documentRepository.updateName(name, id);
        }
        throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
    }

    public String getContent(Long id) {
        return documentsContentChanges.get(id);
    }

    public void updateContent(Log log) {
        if(! documentRepository.findById(log.getDocumentId()).isPresent()){
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
     * @param parentFolderId - the folder that document is located.
     * @param id             - document id.
     * @return rows affected in mysql.
     */
    public int relocate(Long parentFolderId, Long id) {
        boolean a = folderRepository.findById(id).isPresent();
        boolean b = documentRepository.findById(id).isPresent();
        if (!a) {
            throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
        }
        if (!b) {
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
        }
        return documentRepository.updateParentFolderId(parentFolderId, id);
    }

    /**
     * delete file by getting the document id,
     * also remove from the maps of content we have on service.
     * @param docId - gets document id .
     */
    public void delete(Long docId) {
        databaseDocumentsContent.remove(docId);
        documentsContentChanges.remove(docId);
        documentRepository.deleteById(docId);
    }
}
