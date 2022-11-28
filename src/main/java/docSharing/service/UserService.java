package docSharing.service;

import docSharing.entity.Document;
import docSharing.entity.Permission;
import docSharing.entity.User;
import docSharing.entity.UserDocument;
import docSharing.repository.DocumentRepository;
import docSharing.repository.UserDocumentRepository;
import docSharing.repository.UserRepository;
import docSharing.utils.ExceptionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDocumentRepository userDocumentRepository;
    @Autowired
    private DocumentRepository documentRepository;


    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public int updatePermission(Long docId, Long userId, Permission permission) {
        if (!documentRepository.findById(docId).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
        }
        if (!userRepository.findById(userId).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.NO_ACCOUNT_IN_DATABASE.toString());
        }
        Document doc = documentRepository.findById(docId).get();
        User user = userRepository.findById(userId).get();
        if (userDocumentRepository.find(doc, user).isPresent()) {
            UserDocument userDocument = userDocumentRepository.find(doc, user).get();
            userDocument.setPermission(permission);
            return userDocumentRepository.updatePermission(permission, doc, user);
        }
        throw new IllegalArgumentException(ExceptionMessage.CANT_ASSIGN_PERMISSION.toString());
    }

    public UserDocument givePermission(Long docId, Long userId, Permission permission) {
        if (!documentRepository.findById(docId).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
        }
        if (!userRepository.findById(userId).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.NO_ACCOUNT_IN_DATABASE.toString());
        }
        Document doc = documentRepository.findById(docId).get();
        User user = userRepository.findById(userId).get();
        UserDocument userDocument=new UserDocument();
        userDocument.setUser(user);
        userDocument.setDocument(doc);
        userDocument.setPermission(permission);
        return userDocumentRepository.save(userDocument);
    }


}
