package docSharing.service;

import docSharing.entity.Document;
import docSharing.entity.Permission;
import docSharing.entity.User;
import docSharing.entity.UserDocument;
import docSharing.repository.DocumentRepository;
import docSharing.repository.UserDocumentRepository;
import docSharing.repository.UserRepository;
import docSharing.requests.Type;
import docSharing.response.FileRes;
import docSharing.response.UserRes;
import docSharing.utils.ExceptionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.security.auth.login.AccountNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDocumentRepository userDocumentRepository;
    @Autowired
    private DocumentRepository documentRepository;

    /**
     * @param id - user's id
     * @return entity of user that found in database.
     */
    public User findById(Long id) throws AccountNotFoundException {
        if (!userRepository.findById(id).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_ACCOUNT_IN_DATABASE.toString());
        return userRepository.findById(id).get();
    }

    /**
     * @param email - user's email
     * @return entity of user that found in database.
     */
    public User findByEmail(String email) throws AccountNotFoundException {
        if (!userRepository.findByEmail(email).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_ACCOUNT_IN_DATABASE + email);
        return userRepository.findByEmail(email).get();
    }

    /**
     * function called to change a user's permission in database.
     * if a record with the given parameters isn't found, create a new one.
     *
     * @param docId      - document's id in database
     * @param userId     - user's id in database
     * @param permission - the new Permission
     */
    public void updatePermission(Long docId, Long userId, Permission permission) {
        if (!documentRepository.findById(docId).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
        }
        if (!userRepository.findById(userId).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.NO_ACCOUNT_IN_DATABASE.toString());
        }
        Document doc = documentRepository.findById(docId).get();
        User user = userRepository.findById(userId).get();
        if (userDocumentRepository.find(doc, user).isPresent()) {
            userDocumentRepository.updatePermission(permission, doc, user);
        } else {
            UserDocument userDocument = new UserDocument();
            userDocument.setUser(user);
            userDocument.setDocument(doc);
            userDocument.setPermission(permission);
            userDocumentRepository.save(userDocument);
        }
    }

    /**
     * function get called by controller from GET method to get all UserDocument.
     *
     * @param userId - user's id
     * @return list of UserDocument
     */
    public List<FileRes> documentsOfUser(Long userId) {
        if (!userRepository.findById(userId).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.NO_ACCOUNT_IN_DATABASE.toString());
        }
        List<UserDocument> ud = userDocumentRepository.findByUser(userRepository.findById(userId).get());
        List<FileRes> userDocumentResList = new ArrayList<>();
        for (UserDocument userDocument :
                ud) {
            if(userDocument.getPermission()!=Permission.ADMIN)
            userDocumentResList.add(new FileRes(userDocument.getDocument().getName(),userDocument.getDocument().getId(), Type.DOCUMENT, userDocument.getPermission(), userDocument.getDocument().getUser().getEmail()));
        }
        return userDocumentResList;
    }

    public UserRes getUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.NO_ACCOUNT_IN_DATABASE.toString());
        }
        return new UserRes(user.get().getName(), user.get().getEmail(), user.get().getId());
    }

}
