package docSharing.service;

import docSharing.entity.Document;
import docSharing.entity.GeneralItem;
import docSharing.entity.Folder;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.utils.ExceptionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DocumentService implements ServiceInterface {

    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    FolderRepository folderRepository;


    public Document getDocById(Long id){
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

    public int updateContent(String documentContent, Long documentId){
        return documentRepository.updateContent(documentContent, documentId);
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
     * delete file by getting the document id.
     *
     * @param docId - gets document id .
     */
    public void delete(Long docId) {
        documentRepository.deleteById(docId);
    }
}
