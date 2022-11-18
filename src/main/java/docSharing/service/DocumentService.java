package docSharing.service;

import docSharing.entity.Document;
import docSharing.entity.User;
import docSharing.repository.DocumentRepository;
import docSharing.utils.ExceptionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DocumentService implements ServiceInterface{

    @Autowired
    DocumentRepository documentRepository;

    public Long create(Long userId, String name, Long folderId) {
        if (folderId != null) {
            Optional<Document> doc = documentRepository.findById(folderId);
            if (!doc.isPresent())
                throw new IllegalArgumentException(ExceptionMessage.FOLDER_EXISTS.toString() + folderId);
        }

        return documentRepository.save(Document.createDocument(userId, name, folderId)).getId();
    }

    public int rename(Long id, String name) {
        // TODO: make sure this folder exists in the db
        return documentRepository.updateName(name, id);
    }

    public int relocate(Long parentFolderId, Long id) {
        // TODO: make sure both folders exist in the db
        return documentRepository.updateParentFolderId(parentFolderId, id);
    }

    public void delete(Long docId) {
        // TODO: make sure this folder exists in the db
        documentRepository.deleteById(docId);
    }
}
