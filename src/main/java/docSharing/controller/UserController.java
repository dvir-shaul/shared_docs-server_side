package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Permission;
import docSharing.entity.User;
import docSharing.response.JoinRes;
import docSharing.response.Response;
import docSharing.service.DocumentService;
import docSharing.service.EmailService;
import docSharing.service.UserService;
import docSharing.utils.Invite;
import docSharing.utils.Share;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.security.auth.login.AccountNotFoundException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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
     * givePermission is a PATCH function for changing the user role.
     * @param documentId - the document id in database
     * @param uid - the user id in database that will change his permission
     * @param permission - the new permission
     * @param userId - the user id that sent this request
     * @return ResponseEntity with the message, if it worked or not.
     */
    @RequestMapping(value = "/permission/give", method = RequestMethod.PATCH)
    public ResponseEntity<Response> givePermission(@RequestParam Long documentId, @RequestParam Long uid, @RequestParam Permission permission, @RequestAttribute Long userId) {
        logger.info("in UserController -> givePermission");
        if (documentId == null || uid == null || permission == null) {
            logger.error("in UserController -> givePermission -> on of documentId,uid,permission is null");
            return new ResponseEntity<>(new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .message("Could not continue due to lack of data. Required: documentId, uid, permission")
                    .build(), HttpStatus.BAD_REQUEST);
        }
        try {
            return new ResponseEntity<>(new Response.Builder()
                    .data(documentService.getAllUsersInDocument(documentId))
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .message("Successfully changed permission to user id:" + uid)
                    .build(), HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .message(e.getMessage())
                    .build(), HttpStatus.BAD_REQUEST);
        } catch (AccountNotFoundException e) {
            logger.error("in UserController -> givePermission -> " +e.getMessage());
            throw new RuntimeException(e);
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
    // FIXME: Move to a different place. Too much logics for a controller!
    //  maybe even take it out to a private separate function...

    @RequestMapping(value = "/share", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Response> givePermissionToAll(@RequestBody List<String> emails, @RequestParam Long documentId, @RequestAttribute Long userId) {
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

                for (String unregisteredEmail : unregisteredUsers) {
                    String inviteUserString = Invite.emailBody;
                    emailService.send(unregisteredEmail, inviteUserString, "Personal invitation");
                }
            }
            return new ResponseEntity<>(new Response.Builder()
                    .statusCode(200)
                    .status(HttpStatus.OK)
                    .data(documentService.getAllUsersInDocument(documentId))
                    .build(), HttpStatus.OK);

        } catch (MessagingException | IOException | AccountNotFoundException e) {
            logger.error("in UserController -> givePermissionToAll -> " +e.getMessage());
            return new ResponseEntity<>(new Response.Builder()
                    .message(e.getMessage())
                    .statusCode(400)
                    .status(HttpStatus.BAD_REQUEST)
                    .build(), HttpStatus.BAD_REQUEST);
        }
    }
    /**
     * getDocuments is a GET method that sends all the document the user have linked with him.
     * @param userId - id in database.
     * @return - list with document.
     */
    @RequestMapping(value = "sharedDocuments", method = RequestMethod.GET)
    public ResponseEntity<Response> getDocuments(@RequestAttribute Long userId) {
        return new ResponseEntity<>(new Response.Builder()
                .data(userService.documentsOfUser(userId))
                .message("Successfully managed to fetch all shared documents for a user!")
                .statusCode(200)
                .status(HttpStatus.OK)
                .build(), HttpStatus.OK);
    }
    /**
     * getUser is a GET method that sends an entity of user to the client.
     * @param userId - user's id in database.
     * @return - entity of UserRes that's contain name,email and id.
     */
    @RequestMapping(value = "getUser", method = RequestMethod.GET)
    public ResponseEntity<Response> getUser(@RequestAttribute Long userId) {
        return new ResponseEntity<>(new Response.Builder()
                .data(userService.getUser(userId))
                .status(HttpStatus.OK)
                .statusCode(200)
                .message("Successfully managed to get the user from the database.")
                .build(),HttpStatus.OK);
    }

    /**
     *
     * @param documentId
     * @param userId
     * @return
     */
    @RequestMapping(value = "document/getUser", method = RequestMethod.GET)
    public ResponseEntity<Response> getUserPermissionForSpecificDocument(@RequestParam Long documentId, @RequestAttribute Long userId) {
        try {
            User user = userService.findById(userId);
            Permission permission = documentService.getUserPermissionInDocument(userId, documentId);
            return new ResponseEntity<>(new Response.Builder()
                    .data(new JoinRes(user.getName(), userId, permission))
                    .message("Successfully managed to fetch a user with his permission")
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .build(),HttpStatus.OK);

        } catch (AccountNotFoundException e) {
            return new ResponseEntity<>(new Response.Builder()
                    .statusCode(400)
                    .status(HttpStatus.BAD_REQUEST)
                    .message(e.getMessage())
                    .build(),HttpStatus.BAD_REQUEST);
        }
    }
}
