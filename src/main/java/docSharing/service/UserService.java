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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.security.auth.login.AccountNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static Logger logger = LogManager.getLogger(UserService.class.getName());

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDocumentRepository userDocumentRepository;
    @Autowired
    private DocumentRepository documentRepository;

    /**
     * findById search in the database for a user based on the id we have.
     *
     * @param id - user's id
     * @return entity of user that found in database.
     */
    public User findById(Long id) throws AccountNotFoundException {
        logger.info("in UserService -> findById");
        Optional<User> user = userRepository.findById(id);
        if (!user.isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_ACCOUNT_IN_DATABASE.toString());
        return user.get();
    }

    /**
     * findByEmail search in the database for a user based on the email we have.
     *
     * @param email - user's email
     * @return entity of user that found in database.
     */
    public User findByEmail(String email) throws AccountNotFoundException {
        logger.info("in UserService -> findByEmail");
        Optional<User> user = userRepository.findByEmail(email);
        if (!user.isPresent()) {
            logger.error("in UserService -> findByEmail --> " + ExceptionMessage.NO_ACCOUNT_IN_DATABASE + email);
            throw new AccountNotFoundException(ExceptionMessage.NO_ACCOUNT_IN_DATABASE + email);
        }
        return user.get();
    }

    /**
     * function called to change a user's permission in database.
     * checks if the document exist.
     * checks if user exist.
     * checks if permission is UNAUTHORIZED then delete from document.
     * if a record with the given parameters isn't found, create a new one.
     *
     * @param docId      - document's id in database
     * @param userId     - user's id in database
     * @param permission - the new Permission
     */
    public void updatePermission(Long docId, Long userId, Permission permission) {
        logger.info("in UserService -> updatePermission");
        Optional<Document> document = documentRepository.findById(docId);
        if (!document.isPresent()) {
            logger.error("in UserService -> updatePermission --> " + ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS);
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
        }
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            logger.error("in UserService -> updatePermission --> " + ExceptionMessage.NO_ACCOUNT_IN_DATABASE);
            throw new IllegalArgumentException(ExceptionMessage.NO_ACCOUNT_IN_DATABASE.toString());
        }
        if (permission.equals(Permission.UNAUTHORIZED)) {
            userDocumentRepository.deleteUserFromDocument(user.get(), document.get());
            return;
        }
        Optional<UserDocument> optUserDocument = userDocumentRepository.find(document.get(), user.get());
        if (optUserDocument.isPresent()) {
            userDocumentRepository.updatePermission(permission, document.get(), user.get());
        } else {
            UserDocument userDocument = new UserDocument();
            userDocument.setUser(user.get());
            userDocument.setDocument(document.get());
            userDocument.setPermission(permission);
            userDocumentRepository.save(userDocument);
        }
    }

    /**
     * function get called by controller from GET method sharedDocuments path,
     * to get all files that connected to user's specific id.
     *
     * @param userId - user's id
     * @return list of FileRes
     */
    public List<FileRes> documentsOfUser(Long userId) {
        logger.info("in UserService -> documentsOfUser");
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            logger.error("in UserService -> documentsOfUser --> " + ExceptionMessage.NO_ACCOUNT_IN_DATABASE);
            throw new IllegalArgumentException(ExceptionMessage.NO_ACCOUNT_IN_DATABASE.toString());
        }
        List<UserDocument> userDocuments = userDocumentRepository.findByUser(user.get());
        List<FileRes> userDocumentResList = new ArrayList<>();
        for (UserDocument userDocument : userDocuments) {
            if (userDocument.getPermission() != Permission.ADMIN) {
                userDocumentResList.add(new FileRes(userDocument.getDocument().getName(), userDocument.getDocument().getId(), Type.DOCUMENT, userDocument.getPermission(), userDocument.getDocument().getUser().getEmail()));
            }
        }
        return userDocumentResList;
    }

    /**
     * getUser called from userController to send back to client an UserRes which is a response
     * with name mail and id.
     *
     * @param userId - user id in database.
     * @return - entity of UserRes that's contain name,email and id.
     */
    public UserRes getUser(Long userId) {
        logger.info("in UserService -> getUser");

        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            logger.error("in UserService -> getUser --> " + ExceptionMessage.NO_ACCOUNT_IN_DATABASE);
            throw new IllegalArgumentException(ExceptionMessage.NO_ACCOUNT_IN_DATABASE.toString());
        }
        return new UserRes(user.get().getName(), user.get().getEmail(), user.get().getId());
    }

}
