package docSharing.controller;

import docSharing.entity.Permission;
import docSharing.entity.User;
import docSharing.requests.GivePermissionReq;
import docSharing.service.UserService;
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

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<User> getUserById(@RequestParam int id){
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value="/delete/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable("id") int id){
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value="/permission", method = RequestMethod.POST)
    public ResponseEntity<?> givePermission(@RequestBody GivePermissionReq givePermissionReq){
        if(givePermissionReq.getDocumentId() == null || givePermissionReq.getUserId() == null || givePermissionReq.getPermission()==null){
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.ok().body(String.valueOf(userService.givePermission(givePermissionReq.getDocumentId(),givePermissionReq.getUserId(),givePermissionReq.getPermission())));
        }
        catch (IllegalArgumentException exception){
            return ResponseEntity.badRequest().body(exception.getMessage());

        }

    }

}
