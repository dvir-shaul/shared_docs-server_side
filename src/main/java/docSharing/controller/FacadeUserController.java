package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Permission;
import docSharing.entity.User;
import docSharing.requests.Method;
import docSharing.response.FileRes;
import docSharing.response.JoinRes;
import docSharing.response.Response;
import docSharing.response.UserRes;
import docSharing.service.DocumentService;
import docSharing.service.UserService;
import docSharing.utils.EmailUtil;
import docSharing.utils.Invite;
import docSharing.utils.Share;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.security.auth.login.AccountNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class FacadeUserController {
    @Autowired
    DocumentService documentService;
    @Autowired
    UserService userService;
    private static Logger logger = LogManager.getLogger(FacadeUserController.class.getName());

    /**
     * givePermission is a method when the admin of a document want to change the
     * permission of a user in a specific document.
     *
     * @param documentId - document id in the database.
     * @param userId     - user id in the database.
     * @param permission - new permission from: VIEWER, EDITOR, MODERATOR,ADMIN,UNAUTHORIZED
     * @return - Response with status code, and send back all the live users of a document.
     */
    public Response givePermission(Long documentId, Long userId, Permission permission) {
        logger.info("in FacadeUserController -> givePermission in documentId:"
                + documentId + ", userId:" + userId + ", permission:" + permission);
        // FIXME: use Validations.validate for it.
        if (documentId == null || userId == null || permission == null) {
            logger.error("in FacadeUserController -> givePermission -> on of documentId,uid,permission is null");
            return new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .message("Could not continue due to lack of data. Required: documentId, uid, permission")
                    .build();
        }
        try {
            userService.updatePermission(documentId, userId, permission);
            return new Response.Builder()
                    .data(documentService.getAllUsersInDocument(userId, documentId, Method.GET))
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .message("Successfully changed permission to user id:" + userId)
                    .build();
        } catch (AccountNotFoundException e) {
            logger.error("in FacadeUserController -> givePermission -> " + e.getMessage());
            return new Response.Builder()
                    .message("failed to update a permission")
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .build();
        }
    }

    /**
     * givePermissionToAll is called from UserController when we need to share a document to
     * a list of emails, if the email is not exist in the database we save him in the unregisteredUsers,
     * and send activation link to all of them.
     *
     * @param emails     - list of emails.
     * @param documentId - document id in the database.
     * @return - Response with status code, and send back all the live users of a document.
     */
    public Response givePermissionToAll(List<String> emails, Long documentId) {
        logger.info("in FacadeUserController -> givePermissionToAll in documentId:" + documentId);
        try {
            List<String> unregisteredUsers = new ArrayList<>();
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
                    EmailUtil.send(user.getEmail(), body, "You have been invited to view the document");
                }
            }
            for (String unregisteredEmail : unregisteredUsers) {
                String inviteUserString = Invite.emailBody;
                EmailUtil.send(unregisteredEmail, inviteUserString, "Personal invitation");
            }
            return new Response.Builder()
                    .statusCode(200)
                    .status(HttpStatus.OK)
                    .data(documentService.getAllUsersInDocument(null, documentId, Method.GET))
                    .build();
        } catch (MessagingException | IOException | AccountNotFoundException e) {
            logger.error("in FacadeUserController -> givePermissionToAll -> " + e.getMessage());
            return new Response.Builder()
                    .message(e.getMessage())
                    .statusCode(400)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

    }

    /**
     * getDocuments is called from UserController when we want to send back to the client the list
     * of FileRes that belong to the given user.
     *
     * @param userId - user id in the database.
     * @return - Response with status code, and the data is the docsOfUser.
     */
    public Response getDocuments(Long userId) {
        logger.info("in FacadeUserController -> getDocuments for userId:" + userId);
        try {
            List<FileRes> docsOfUser = userService.documentsOfUser(userId);
            return new Response.Builder()
                    .data(docsOfUser)
                    .message("Successfully managed to fetch all shared documents for a user!")
                    .statusCode(200)
                    .status(HttpStatus.OK)
                    .build();
        } catch (IllegalArgumentException e) {
            logger.error("in FacadeUserController -> givePermissionToAll -> IllegalArgumentException->" + e.getMessage());
            return new Response.Builder()
                    .data("")
                    .message(e.getMessage())
                    .statusCode(400)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    /**
     * getUser is called from UserController when we want to send back to the client
     * the endpoint of user with name,email and id.
     *
     * @param userId - user id in the database.
     * @return - Response with status code, and the data is entity of UserRes that's contain name,email and id.
     */
    public Response getUser(Long userId) {
        logger.info("in FacadeUserController -> getUser");
        try {
            UserRes userRes = userService.getUser(userId);
            return new Response.Builder()
                    .data(userRes)
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .message("Successfully managed to get the user from the database.")
                    .build();
        } catch (IllegalArgumentException e) {
            logger.error("in FacadeUserController -> getUser -> IllegalArgumentException->" + e.getMessage());

            return new Response.Builder()
                    .data("")
                    .message(e.getMessage())
                    .statusCode(400)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    /**
     * getUserPermissionForSpecificDocument is called from UserController when we need to get
     * the user permission for the client.
     *
     * @param documentId - document id in the database.
     * @param userId     - user id in the database.
     * @return -  Response with status code, and the data is JoinRes entity that contain the name, permission and id of a user.
     */
    public Response getUserPermissionForSpecificDocument(Long documentId, Long userId) {
        logger.info("in FacadeUserController -> getUserPermissionForSpecificDocument");
        try {
            User user = userService.findById(userId);
            Permission permission = documentService.getUserPermissionInDocument(userId, documentId);
            return new Response.Builder()
                    .data(new JoinRes(user.getName(), userId, permission))
                    .message("Successfully managed to fetch a user with his permission")
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .build();
        } catch (AccountNotFoundException e) {
            logger.error("in FacadeUserController -> getUserPermissionForSpecificDocument -> AccountNotFoundException->" + e.getMessage());
            return new Response.Builder()
                    .statusCode(400)
                    .status(HttpStatus.BAD_REQUEST)
                    .message(e.getMessage())
                    .build();
        }
    }
}
