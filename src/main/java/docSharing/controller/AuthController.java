package docSharing.controller;

import docSharing.entity.User;
import docSharing.utils.Regex;
import docSharing.utils.Validations;
import docSharing.service.AuthService;
import docSharing.service.EmailService;
import docSharing.service.token.ConfirmationToken;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Date;

@Controller
@RequestMapping(value = "/user/auth")
@AllArgsConstructor
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private EmailService emailService;

    /**
     * Register function is responsible for creating new users and adding them to the database.
     * Users will use their personal information to create a new account: email, password, name.
     * @param user
     */
    @RequestMapping(value = "register", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<String> register(@RequestBody User user) {
        String email = user.getEmail();
        String name = user.getName();
        String password = user.getPassword();

        // make sure we got all the data from the client
        if (name == null || email == null || password == null || user.getId() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You must include all and exact parameters for such an action: email, name, password");
        }

        // validate information
        try {
            Validations.validate(Regex.EMAIL.getRegex(), email);
            Validations.validate(Regex.PASSWORD.getRegex(), password);
//            Validations.validate(Regex.NAME.getRegex(), name);
            authService.register(email, password, name);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }

        // if correct -> call auth service with parameters -> register function
        return ResponseEntity.status(200).body("Account has been successfully registered and created!");
    }

    /**
     * Login function is responisble for logging user into the system.
     * This function accepts only 2 parameters: email, password.
     * If the credentials match to the database's information, it will allow the user to use its functionalities.
     * A token will be returned in a successful request.
     * @param user
     * @return token
     */
    @RequestMapping(value = "login", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<String> login(@RequestBody User user) {
        String email = user.getEmail();
        String password = user.getPassword();

        // make sure we got all the data from the client
        if (email == null || password == null || user.getId() != null || user.getName() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You must include all and exact parameters for such an action: email, name, password");
        }

        // validate information
        try {
            Validations.validate(Regex.EMAIL.getRegex(), email);
            Validations.validate(Regex.PASSWORD.getRegex(), password);
            authService.login(email, password);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (AccountNotFoundException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        // if correct -> call auth service with parameters -> login function
        return ResponseEntity.status(200).body("this is the token");
    }

    /**
     * Activate function is responsible for activating email links.
     * If the link is not expired, make the user activated in the database.
     * If the link is expired, resend a new link to the user with a new token.
     * @param link - A link with activation token
     */
    @RequestMapping(value = "activate", method = RequestMethod.POST, consumes = "application/json")
    public void activate(@RequestParam String link) {
        // parse link to token
        Claims claim = ConfirmationToken.decodeJWT(link);

        // check if token is still activated
        Boolean isUpToDate = claim.getExpiration().after(new Date());

        // if yes -> call AuthService with activate function
        if (isUpToDate) {
            authService.activate(Long.valueOf(claim.getId()));
            // if no do that -> resend email
        } else {
            emailService.reactivateLink(link);
        }
    }
}
