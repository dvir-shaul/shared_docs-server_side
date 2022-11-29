package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Permission;
import docSharing.entity.User;
import docSharing.requests.UpdatePermissionReq;
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

import java.util.ArrayList;
import java.util.List;


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

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<User> getUserById(@RequestParam int id) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable("id") int id) {
        return ResponseEntity.noContent().build();
    }



    @RequestMapping(value = "/permission/give", method = RequestMethod.PATCH)
    public ResponseEntity<?> givePermission(@RequestBody UpdatePermissionReq permissionReq, @RequestAttribute Long userId) {
        if (permissionReq.getDocumentId() == null || permissionReq.getUserId() == null || permissionReq.getPermission() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (documentService.findById(permissionReq.getDocumentId()).get().getUser().getId() != userId) {
            return ResponseEntity.badRequest().body(ExceptionMessage.USER_IS_NOT_THE_ADMIN);
        }
        try {
            userService.updatePermission(permissionReq.getDocumentId(), permissionReq.getUserId(), permissionReq.getPermission());
            return ResponseEntity.ok().body("permission added successfully!");
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());

        }
    }

    @RequestMapping(value = "/share", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> givePermissionToAll(@RequestBody List<String> emails, @RequestParam Long documentId, @RequestAttribute Long userId) {
       List<String> unregisteredUsers=new ArrayList<>();
        for (String email :
                emails) {
            User user = userService.findByEmail(email);
            if(user==null){
                unregisteredUsers.add(email);
                continue;
            }
            Document document = documentService.findById(documentId).get();
            userService.updatePermission(documentId, user.getId(), Permission.VIEWER);
            String body = Share.buildEmail(user.getName(), "HERE SHOULD BE THE DOC URL", document.getName());
            try {
                emailService.send(user.getEmail(), body,"You have been invited to view the document");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        for (String unregisteredEmail :
                unregisteredUsers) {
            String inviteUserString= Invite.emailBody;
            try {
                emailService.send(unregisteredEmail, inviteUserString, "A personal invitation");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseEntity.ok("doc shared successfully!");
    }
    @RequestMapping(value="documents", method = RequestMethod.GET)
    public ResponseEntity<?> getDocuments(@RequestAttribute Long userId){
        return ResponseEntity.ok(userService.documentsOfUser(userId));
    }
}
