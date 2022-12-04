package docSharing.controller;

import docSharing.entity.User;
import docSharing.response.Response;
import docSharing.service.FolderService;
import docSharing.service.UserService;
import docSharing.utils.*;
import docSharing.service.AuthService;
import docSharing.utils.EmailUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.security.auth.login.AccountNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping(value = "/user/auth")
@AllArgsConstructor
@CrossOrigin
public class AuthController {

    @Autowired
    FacadeController facadeController;

    /**
     * Register function is responsible for creating new users and adding them to the database.
     * Users will use their personal information to create a new account: email, password, name.
     *
     * @param user
     */
    @RequestMapping(value = "register", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Response> register(@RequestBody User user) {
        Response response = facadeController.register(user);
        return new ResponseEntity<>(response, response.getStatus());
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
    public ResponseEntity<Response> login(@RequestBody User user) {
        Response response = facadeController.login(user);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * Activate function is responsible for activating email links.
     * If the link is not expired, make the user activated in the database.
     * If the link is expired, resend a new link to the user with a new token.
     *
     * @param token - A link with activation token
     * @return
     */
    @RequestMapping(value = "activate", method = RequestMethod.POST)
    public ResponseEntity<Response> activate(@RequestParam String token) {
        Response response = facadeController.activate(token);
        return new ResponseEntity<>(response, response.getStatus());
    }
}
