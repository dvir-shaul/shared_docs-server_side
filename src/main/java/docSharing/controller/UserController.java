package docSharing.controller;

import docSharing.entity.User;
import docSharing.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Permission;

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
    public ResponseEntity<Void> givePermission(@RequestParam Long docId, @RequestParam Long userId ,@RequestParam Permission permission){
        // call service and chang permission to the user
        // ...
        return ResponseEntity.ok().build();
    }

}
