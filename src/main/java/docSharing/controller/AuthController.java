package docSharing.controller;

import docSharing.entity.User;
import docSharing.service.UserService;
import docSharing.utils.Activation;
import docSharing.utils.Validations;
import docSharing.utils.Regex;
import docSharing.service.AuthService;
import docSharing.service.EmailService;
import docSharing.utils.ConfirmationToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Controller
@RequestMapping(value = "/user/auth")
@AllArgsConstructor
public class AuthController {


    @Autowired
    private AuthService authService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserService userService;

    /**
     * Register function is responsible for creating new users and adding them to the database.
     * Users will use their personal information to create a new account: email, password, name.
     *
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
            User emailUser = authService.register(email, password, name);
            String token = ConfirmationToken.createJWT(Long.toString(emailUser.getId()), "docs-app", "activation email", 300000);
            String link = Activation.buildLink(token);
            String mail = Activation.buildEmail(emailUser.getName(), link);
            try {
                emailService.send(emailUser.getEmail(), mail);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        // if correct -> call auth service with parameters -> register function
        return ResponseEntity.status(200).body("Account has been successfully registered and created!");
    }

    /**
     * Login function is responsible for logging user into the system.
     * This function accepts only 2 parameters: email, password.
     * If the credentials match to the database's information, it will allow the user to use its functionalities.
     * A token will be returned in a successful request.
     *
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
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        // if correct -> call auth service with parameters -> login function
        return ResponseEntity.status(200).body("this is the token");
    }

    /**
     * Activate function is responsible for activating email links.
     * If the link is not expired, make the user activated in the database.
     * If the link is expired, resend a new link to the user with a new token.
     *
     * @param token - A link with activation token
     * @return
     */
//    @RequestMapping(value = "activate", method = RequestMethod.GET)
//    public ResponseEntity<String> activate(@RequestParam String token) {
//        String parsedToken = null;
//        try {
//            parsedToken = URLDecoder.decode(token, StandardCharsets.UTF_8.toString()).replaceAll(" ", ".");
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e);
//        }
//        try {
//            Claims claim = ConfirmationToken.decodeJWT(parsedToken);
//            Boolean isUpToDate = claim.getExpiration().after(new Date());
//            if (isUpToDate) {
//                authService.activate(Integer.valueOf(claim.getId()));
//            }
//
//        } catch (ExpiredJwtException e) {
//            emailService.reactivateLink(token);
//        }
//        return ResponseEntity.status(200).body("account activated successfully!");
//    }
    @RequestMapping(value = "activate", method = RequestMethod.GET)
    public ResponseEntity<String> activate(@RequestHeader(value = "token") String token) {
        Long id = Long.valueOf(12);
        int rowsAffected = authService.activate(id);
//
//        String parsedToken = null;
//        try {
//            parsedToken = URLDecoder.decode(token, StandardCharsets.UTF_8.toString()).replaceAll(" ", ".");
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e);
//        }
//        try {
//            Claims claim = ConfirmationToken.decodeJWT(parsedToken);
//            Boolean isUpToDate = claim.getExpiration().after(new Date());
//            if (isUpToDate) {
//            }
//
//        } catch (ExpiredJwtException e) {
//            emailService.reactivateLink(token);
//        }
        if (rowsAffected > 0)
            return ResponseEntity.status(200).body("account activated successfully!");
        else return ResponseEntity.status(200).body("No entry was updated for this id: " + id);
    }
}
