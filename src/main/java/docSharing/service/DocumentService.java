package docSharing.service;

import docSharing.entity.Document;
import docSharing.entity.User;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.utils.ExceptionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DocumentService implements ServiceInterface{

    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    FolderRepository folderRepository;

    public Long create(Long userId, String name, Long folderId) {
        if (folderId != null) {
            Optional<Document> doc = documentRepository.findById(folderId);
            if (!doc.isPresent())
                throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString() + folderId);
        }
        return documentRepository.save(Document.createDocument(userId, name, folderId)).getId();
    }

    public int rename(Long id, String name) {
        // TODO: make sure this folder exists in the db
        if(documentRepository.findById(id).isPresent()){
            return documentRepository.updateName(name, id);
        }
        throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
    }

    public int relocate(Long parentFolderId, Long id) {
        // TODO: make sure both folders exist in the db
        boolean a = folderRepository.findById(id).isPresent();
        boolean b = documentRepository.findById(id).isPresent();
        if(! a) {throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());}
        if(! b) {throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());}
        return documentRepository.updateParentFolderId(parentFolderId, id);
    }

    public void delete(Long docId) {
        // TODO: make sure this folder exists in the db
        documentRepository.deleteById(docId);
    }
}
