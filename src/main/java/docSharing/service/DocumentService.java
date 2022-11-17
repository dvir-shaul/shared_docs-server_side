package docSharing.service;

import docSharing.entity.Document;
import docSharing.entity.User;
import docSharing.repository.DocumentRepository;
import docSharing.utils.ExceptionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DocumentService implements ChangesService {

    @Autowired
    DocumentRepository documentRepository;

    public Long create(Long userId, String title, Long folderId) {
        if (folderId != null) {
            Optional<Document> doc = documentRepository.findById(folderId);
            if (!doc.isPresent())
                throw new IllegalArgumentException(ExceptionMessage.FOLDER_EXISTS.toString() + folderId);
        }

        return documentRepository.save(Document.createDocument(userId, title, folderId)).getId();
    }
}
