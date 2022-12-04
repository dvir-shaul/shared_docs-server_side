package docSharing.controller;

import docSharing.entity.Permission;
import docSharing.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {
    @Autowired
    private FacadeController facadeController;

    @RequestMapping(value = "/permission/give", method = RequestMethod.PATCH)
    public ResponseEntity<Response> givePermission(@RequestParam Long documentId, @RequestParam Long uid, @RequestParam Permission permission, @RequestAttribute Long userId) {
        Response response = facadeController.givePermission(documentId, uid, permission);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @RequestMapping(value = "/share", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Response> givePermissionToAll(@RequestBody List<String> emails, @RequestParam Long documentId, @RequestAttribute Long userId) {
        Response response = facadeController.givePermissionToAll(emails, documentId);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @RequestMapping(value = "sharedDocuments", method = RequestMethod.GET)
    public ResponseEntity<Response> getDocuments(@RequestAttribute Long userId) {
        Response response = facadeController.getDocuments(userId);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @RequestMapping(value = "getUser", method = RequestMethod.GET)
    public ResponseEntity<Response> getUser(@RequestAttribute Long userId) {
        Response response = facadeController.getUser(userId);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @RequestMapping(value = "document/getUser", method = RequestMethod.GET)
    public ResponseEntity<Response> getUserPermissionForSpecificDocument(@RequestParam Long documentId, @RequestAttribute Long userId) {
        Response response = facadeController.getUserPermissionForSpecificDocument(documentId, userId);
        return new ResponseEntity<>(response, response.getStatus());
    }
}
