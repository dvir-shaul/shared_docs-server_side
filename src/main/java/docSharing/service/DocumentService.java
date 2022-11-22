package docSharing.service;

import docSharing.entity.Document;
import docSharing.entity.GeneralItem;
import docSharing.entity.Folder;
import docSharing.entity.Log;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.utils.ExceptionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DocumentService implements ServiceInterface {

    //       <docId, content>
    static Map<Long, String> documentsContentChanges = new HashMap<>();

    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    FolderRepository folderRepository;


    public Document getDocById(Long id) {
        return documentRepository.findById(id).get();
    }

    public Long create(GeneralItem generalItem) {
        if (generalItem.getParentFolderId() != null) {
            Optional<Folder> folder = folderRepository.findById(generalItem.getParentFolderId());
            if (!folder.isPresent())
                throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString() + generalItem.getParentFolderId());
        }

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

    //        return documentRepository.updateContent(documentContent, documentId);
    public void updateContent(Log log) {
     //TODO: check if document id exists
        if (!documentsContentChanges.containsKey(log.getDocumentId()))
            documentsContentChanges.put(log.getDocumentId(), "");
        //FIXME: load document content from db instead of empty string

        if (log.getAction().equals("delete")) deleteText(log);
        if (log.getAction().equals("insert")) insertText(log);
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

    //TODO: create function that calls the db and saves the content every x seconds

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
     * delete file by getting the document id.
     *
     * @param docId - gets document id .
     */
    public void delete(Long docId) {
        documentRepository.deleteById(docId);
    }
}
