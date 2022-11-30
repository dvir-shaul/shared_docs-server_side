package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Permission;
import docSharing.entity.User;
import docSharing.entity.UserDocument;
import docSharing.response.UsersInDocRes;
import docSharing.service.DocumentService;
import docSharing.service.EmailService;
import docSharing.service.UserService;
import docSharing.utils.ExceptionMessage;
import docSharing.utils.Invite;
import docSharing.utils.Share;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private EmailService emailService;


    @RequestMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable("id") int id) {
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/permission/give", method = RequestMethod.PATCH)
    public ResponseEntity<?> givePermission(@RequestParam Long documentId, @RequestParam Long uid, @RequestParam Permission permission, @RequestAttribute Long userId) {
        if (documentId == null || uid == null || permission == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            if (!Objects.equals(documentService.findById(documentId).getUser().getId(), userId)) {
                return ResponseEntity.badRequest().body(ExceptionMessage.USER_IS_NOT_THE_ADMIN);
            }
            userService.updatePermission(documentId, uid, permission);
            return ResponseEntity.ok().body("permission added successfully!");
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @RequestMapping(value = "/share", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> givePermissionToAll(@RequestBody List<String> emails, @RequestParam Long documentId, @RequestAttribute Long userId) {
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
                Permission permission=documentService.getUserPermissionInDocument(user.getId(), documentId);
                if(permission.equals(Permission.UNAUTORIZED)) {
                    Document document = documentService.findById(documentId);
                    userService.updatePermission(documentId, user.getId(), Permission.VIEWER);
                    String link = "http://localhost:3000/document/share/documentId=" + documentId + "&userId=" + user.getId();
                    String body = Share.buildEmail(user.getName(), link, document.getName());
                    emailService.send(user.getEmail(), body, "You have been invited to view the document");
                }
            }
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (String unregisteredEmail : unregisteredUsers) {
            String inviteUserString = Invite.emailBody;
            try {
                emailService.send(unregisteredEmail, inviteUserString, "Personal invitation");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        try {
            List<UserDocument> usersInDocument = documentService.getAllUsersInDocument(documentId);
            List<UsersInDocRes> usersInDocRes = usersInDocument.stream().map(u -> new UsersInDocRes(u.getUser().getId(), u.getUser().getName(), u.getUser().getEmail(), u.getPermission())).collect(Collectors.toList());
            return ResponseEntity.ok(usersInDocRes);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @RequestMapping(value = "/sharedDocuments", method = RequestMethod.GET)
    public ResponseEntity<?> getDocuments(@RequestAttribute Long userId) {
        return ResponseEntity.ok(userService.documentsOfUser(userId));
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getUser(@RequestAttribute Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }
}
