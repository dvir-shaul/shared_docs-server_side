package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Permission;
import docSharing.entity.User;
import docSharing.entity.UserDocument;
import docSharing.requests.OnlineUsersReq;
import docSharing.response.AllUsers;
import docSharing.response.UserStatus;
import docSharing.response.UsersInDocRes;
import docSharing.service.DocumentService;
import docSharing.service.EmailService;
import docSharing.service.UserService;
import docSharing.utils.ExceptionMessage;
import docSharing.utils.Invite;
import docSharing.utils.Share;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    private static Logger logger = LogManager.getLogger(UserController.class.getName());

    @Autowired
    private UserService userService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private EmailService emailService;

    /**
     * deleteUserById not used
     * @param id - users id
     * @return -
     */
    @RequestMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable("id") int id) {
        return ResponseEntity.noContent().build();
    }

    /**
     * givePermission is a PATCH function for changing the user role.
     * @param documentId - the document id in database
     * @param uid - the user id in database that will change his permission
     * @param permission - the new permission
     * @param userId - the user id that sent this request
     * @return ResponseEntity with the message, if it worked or not.
     */
    @RequestMapping(value = "/permission/give", method = RequestMethod.PATCH)
    public ResponseEntity<?> givePermission(@RequestParam Long documentId, @RequestParam Long uid, @RequestParam Permission permission, @RequestAttribute Long userId) {
        logger.info("in UserController -> givePermission");
        if (documentId == null || uid == null || permission == null) {
            logger.error("in UserController -> givePermission -> on of documentId,uid,permission is null");
            return ResponseEntity.badRequest().build();
        }
        try {
            // FIXME: should be in the filter -> permission filter
            if (!Objects.equals(documentService.findById(documentId).getUser().getId(), userId)) {
                logger.warn("in UserController -> givePermission -> " +ExceptionMessage.USER_IS_NOT_THE_ADMIN);
                return ResponseEntity.badRequest().body(ExceptionMessage.USER_IS_NOT_THE_ADMIN);
            }
            userService.updatePermission(documentId, uid, permission);
            Set<Long> onlineUsers = documentService.getActiveUsersPerDoc(documentId).stream().map(u->u.getId()).collect(Collectors.toSet());
            List<UsersInDocRes> usersInDocRes = documentService.getAllUsersInDocument(documentId).stream().map(u -> new UsersInDocRes(u.getUser().getId(), u.getUser().getName(), u.getUser().getEmail(), u.getPermission(), onlineUsers.contains(u.getUser().getId()) ? UserStatus.ONLINE : UserStatus.OFFLINE)).collect(Collectors.toList());
            return ResponseEntity.ok().body(usersInDocRes);
        } catch (AccountNotFoundException e) {
            logger.error("in UserController -> givePermission -> " +e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException exception) {
            logger.error("in UserController -> givePermission -> " +exception.getMessage());
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    /**
     * givePermissionToAll is a POST function for sharing a document with given a list of emails.
     * if the user's email is not in our database it will send him an invitation to register the app.
     * @param emails - list with emails to share the document.
     * @param documentId - document id in database.
     * @param userId - the user that sends this request
     * @return ResponseEntity with a message.
     */
    @RequestMapping(value = "/share", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> givePermissionToAll(@RequestBody List<String> emails, @RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in UserController -> givePermissionToAll");
        List<String> unregisteredUsers = new ArrayList<>();
        try {
            for (String email : emails) {
                User user = null;
                try {
                    user = userService.findByEmail(email);
                } catch (AccountNotFoundException exception) {
                    unregisteredUsers.add(email);
                    continue;
                }
                Permission permission = documentService.getUserPermissionInDocument(user.getId(), documentId);
                if (permission.equals(Permission.UNAUTHORIZED)) {
                    Document document = documentService.findById(documentId);
                    userService.updatePermission(documentId, user.getId(), Permission.VIEWER);
                    String link = "http://localhost:3000/document/share/documentId=" + documentId + "&userId=" + user.getId();
                    String body = Share.buildEmail(user.getName(), link, document.getName());
                    emailService.send(user.getEmail(), body, "You have been invited to view the document");
                }
            }
        } catch (AccountNotFoundException e) {
            logger.error("in UserController -> givePermissionToAll -> " +e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("in UserController -> givePermissionToAll -> " +e.getMessage());
            throw new RuntimeException(e);
        }

        for (String unregisteredEmail : unregisteredUsers) {
            String inviteUserString = Invite.emailBody;
            try {
                emailService.send(unregisteredEmail, inviteUserString, "Personal invitation");
            } catch (Exception e) {
                logger.error("in UserController -> givePermissionToAll -> " +e.getMessage());
                throw new RuntimeException(e);
            }
        }
        try {
            List<UserDocument> usersInDocument = documentService.getAllUsersInDocument(documentId);
            Set<User> onlineUsers = documentService.getActiveUsersPerDoc(documentId);
            List<UsersInDocRes> usersInDocRes = documentService.getAllUsersInDocument(documentId).stream().map(u -> new UsersInDocRes(u.getUser().getId(), u.getUser().getName(), u.getUser().getEmail(), u.getPermission(), onlineUsers.contains(u.getUser().getId()) ? UserStatus.ONLINE : UserStatus.OFFLINE)).collect(Collectors.toList());

            // List<UsersInDocRes> usersInDocRes = usersInDocument.stream().map(u -> new UsersInDocRes(u.getUser().getId(), u.getUser().getName(), u.getUser().getEmail(), u.getPermission())).collect(Collectors.toList());
            return ResponseEntity.ok(usersInDocRes);
        } catch (AccountNotFoundException e) {
            logger.error("in UserController -> givePermissionToAll -> " +e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * getDocuments is a GET method that sends all the document the user have linked with him.
     * @param userId - id in database.
     * @return - list with document.
     */
    @RequestMapping(value = "sharedDocuments", method = RequestMethod.GET)
    public ResponseEntity<?> getDocuments(@RequestAttribute Long userId) {
        logger.info("in UserController -> getDocuments");
        return ResponseEntity.ok(userService.documentsOfUser(userId));
    }

    /**
     * getUser is a GET method that sends an entity of user to the client.
     * @param userId - user's id in database.
     * @return - entity of UserRes that's contain name,email and id.
     */
    @RequestMapping(value = "getUser", method = RequestMethod.GET)
    public ResponseEntity<?> getUser(@RequestAttribute Long userId) {
        logger.info("in UserController -> getUser");
        return ResponseEntity.ok(userService.getUser(userId));
    }
}
