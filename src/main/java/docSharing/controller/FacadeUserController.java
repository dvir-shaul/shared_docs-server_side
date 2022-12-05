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

    public Response givePermission(long documentId, long userId, Permission permission) {
        // FIXME: use Valdidations.validate for it.
        if (permission == null) {
            logger.error("in UserController -> givePermission -> on of documentId,uid,permission is null");
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
            return new Response.Builder()
                    .message("failed to update a permission")
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .build();
        }
    }

    public Response givePermissionToAll(List<String> emails, long documentId) {
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
                // make sure that in the correct email format -> otherwise skip.
                String inviteUserString = Invite.emailBody;
                EmailUtil.send(unregisteredEmail, inviteUserString, "Personal invitation");
            }
            return new Response.Builder()
                    .statusCode(200)
                    .status(HttpStatus.OK)
                    .data(documentService.getAllUsersInDocument(null, documentId, Method.GET))
                    .build();
        } catch (MessagingException | IOException | AccountNotFoundException e) {
            logger.error("in UserController -> givePermissionToAll -> " + e.getMessage());
            return new Response.Builder()
                    .message(e.getMessage())
                    .statusCode(400)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

    }

    public Response getDocuments(long userId) {
        try {
            List<FileRes> docsOfUser = userService.documentsOfUser(userId);
            return new Response.Builder()
                    .data(docsOfUser)
                    .message("Successfully managed to fetch all shared documents for a user!")
                    .statusCode(200)
                    .status(HttpStatus.OK)
                    .build();
        } catch (IllegalArgumentException e) {
            return new Response.Builder()
                    .message(e.getMessage())
                    .statusCode(400)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    public Response getUser(long userId) {
        try {
            UserRes userRes = userService.getUser(userId);
            return new Response.Builder()
                    .data(userRes)
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .message("Successfully managed to get the user from the database.")
                    .build();
        } catch (IllegalArgumentException e) {
            return new Response.Builder()
                    .message(e.getMessage())
                    .statusCode(400)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    public Response getUserPermissionForSpecificDocument(long documentId, long userId) {
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
            return new Response.Builder()
                    .statusCode(400)
                    .status(HttpStatus.BAD_REQUEST)
                    .message(e.getMessage())
                    .build();
        }
    }
}
