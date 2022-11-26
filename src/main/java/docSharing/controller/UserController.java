package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.User;
import docSharing.requests.UpdatePermissionReq;
import docSharing.service.DocumentService;
import docSharing.service.UserService;
import docSharing.utils.ExceptionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private DocumentService documentService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<User> getUserById(@RequestParam int id) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable("id") int id) {
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/permission", method = RequestMethod.PATCH)
    public ResponseEntity<?> updatePermission(@RequestBody UpdatePermissionReq updatePermissionReq, @RequestAttribute Long userId) {
        if (updatePermissionReq.getDocumentId() == null || updatePermissionReq.getUserId() == null || updatePermissionReq.getPermission() == null) {
            return ResponseEntity.badRequest().build();
        }
        if(documentService.findById(updatePermissionReq.getDocumentId()).get().getUser().getId()!=userId){
            return ResponseEntity.badRequest().body(ExceptionMessage.USER_IS_NOT_THE_ADMIN);
        }
        try {
            return ResponseEntity.ok().body(String.valueOf(userService.updatePermission(updatePermissionReq.getDocumentId(), updatePermissionReq.getUserId(), updatePermissionReq.getPermission())));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());

        }

    }
//
//    @RequestMapping(value = "/permission", method = RequestMethod.PATCH)
//    public ResponseEntity<?> givePermission(@RequestBody UpdatePermissionReq permissionReq, @RequestAttribute Long userId) {
//        if (permissionReq.getDocumentId() == null || permissionReq.getUserId() == null || permissionReq.getPermission() == null) {
//            return ResponseEntity.badRequest().build();
//        }
//            if(documentService.findById(permissionReq.getDocumentId()).get().getUser().getId()!=userId){
//                return ResponseEntity.badRequest().body(ExceptionMessage.USER_IS_NOT_THE_ADMIN);
//            }
//
//        try {
//            return ResponseEntity.ok().body(String.valueOf(userService.givePermission(permissionReq.getDocumentId(), permissionReq.getUserId(), permissionReq.getPermission())));
//        } catch (IllegalArgumentException exception) {
//            return ResponseEntity.badRequest().body(exception.getMessage());
//
//        }
//    }
}
