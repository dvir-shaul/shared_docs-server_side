package docSharing.controller;

import docSharing.Entities.User;
import docSharing.Utils.Activation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import docSharing.service.AuthService;
import docSharing.service.EmailService;
import docSharing.service.token.ConfirmationToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping(value = "/user/auth")
@AllArgsConstructor
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private EmailService emailService;
    @RequestMapping(value = "register", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<String> register(@RequestBody User user) {
        String email = user.getEmail();
        String name = user.getName();
        String password = user.getPassword();

        // make sure we got all the data from the client
        if (name == null || email == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You must include all parameters for such an action: email, name, password");
        }

        String token = ConfirmationToken.createJWT(Integer.toString(user.getId()), "docs-app", "activation email", 300000);
        String link = Activation.buildLink(token);
        String mail = Activation.buildEmail(user.getName(), link);
        try {
            emailService.send(user.getEmail(), mail);
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    @RequestMapping(value="activate", method = RequestMethod.GET)
    public ResponseEntity<String> activate(@RequestParam String token) {
        String parsedToken= null;
        try {
            parsedToken = URLDecoder.decode(token, StandardCharsets.UTF_8.toString()).replaceAll(" ",".");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        try {
            Claims c = ConfirmationToken.decodeJWT(parsedToken);
            authService.activate(Long.parseLong(c.getId()));

        }catch(JwtException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("the link is not valid");
        }

        // parse link to token
        // check if token is still activated
        // if yes -> call AuthService with activate function
        // if no do that -> resend email
        return null;
    }
}
