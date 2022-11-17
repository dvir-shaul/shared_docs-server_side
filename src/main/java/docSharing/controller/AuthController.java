package docSharing.controller;

import docSharing.Entities.User;
import docSharing.Utils.Regex;
import docSharing.Utils.Validations;
import docSharing.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/user/auth")
@AllArgsConstructor
public class AuthController {

    private AuthService authService;

    @RequestMapping(value = "register", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<String> register(@RequestBody User user) {
        String email = user.getEmail();
        String name = user.getName();
        String password = user.getPassword();

        // make sure we got all the data from the client
        if (name == null || email == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You must include all parameters for such an action: email, name, password");
        }

        // validate information
//        try {
//            Validations.validate(Regex.EMAIL.getRegex(), email);
//            Validations.validate(Regex.PASSWORD.getRegex(), password);
////            Validations.validate(Regex.NAME.getRegex(), name);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
//        }

        // if correct -> call auth service with parameters -> register function
        authService.register(email, password, name);
        return null;
    }

    public void login(String email, String password) {
        // validate information
        // if correct -> call authService with parameters -> login function
        // return token
    }

    public void activate() {
        // parse link to token
        // check if token is still activated
        // if yes -> call AuthService with activate function
        // if no do that -> resend email
    }
}
