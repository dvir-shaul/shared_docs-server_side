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
    private AuthService authService;
    @Autowired
    private UserService userService;
    @Autowired
    private FolderService folderService;
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
    public ResponseEntity<Response> activate(@RequestParam String token) throws AccountNotFoundException {
        String parsedToken = null;
        try {
            parsedToken = URLDecoder.decode(token, StandardCharsets.UTF_8.toString()).replaceAll(" ", ".");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        Claims claims = null;
        try {
            claims = ConfirmationToken.decodeJWT(parsedToken);
            if (userService.findById(Long.valueOf(claims.getId())).getActivated()) {
                return new ResponseEntity<>(new Response.Builder()
                        .message("account already activated")
                        .status(HttpStatus.CONFLICT)
                        .statusCode(400)
                        .build(), HttpStatus.CONFLICT);
            }
            authService.activate(Long.valueOf(claims.getId()));
            return new ResponseEntity<>(new Response.Builder()
                    .message("account activated successfully!")
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .build(), HttpStatus.OK);

        } catch (ExpiredJwtException e) {
            String id = e.getClaims().getId();
            User user = userService.findById(Long.valueOf(id));
            EmailUtil.reactivateLink(user);
            return new ResponseEntity<>(new Response.Builder()
                    .message("the link expired, new activation link has been sent")
                    .statusCode(410)
                    .status(HttpStatus.GONE)
                    .build(), HttpStatus.GONE);
        } catch (AccountNotFoundException e) {
            return new ResponseEntity<>(new Response.Builder()
                    .message("You must include all and exact parameters for such an action: email, name, password")
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .build(), HttpStatus.BAD_REQUEST);
        }
    }
}
