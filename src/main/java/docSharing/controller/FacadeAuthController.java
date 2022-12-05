package docSharing.controller;

import docSharing.entity.User;
import docSharing.response.Response;
import docSharing.service.AuthService;
import docSharing.service.FolderService;
import docSharing.service.UserService;
import docSharing.utils.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.security.auth.login.AccountNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class FacadeAuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private FolderService folderService;
    @Autowired
    private UserService userService;
    private static Logger logger = LogManager.getLogger(FacadeAuthController.class.getName());


    public Response register(User user) {
        String email = user.getEmail();
        String name = user.getName();
        String password = user.getPassword();

        // make sure we got all the data from the client
        if (name == null || email == null || password == null ) {
            logger.error("in AuthController -> register -> one of email, name, password is null");
            return new Response.Builder()
                    .message("You must include all and exact parameters for such an action: email, name, password")
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .build();
        }
        try {
//            Validations.validate(Regex.NAME.getRegex(), name);
            Validations.validate(Regex.EMAIL.getRegex(), email);
            Validations.validate(Regex.PASSWORD.getRegex(), password);
            User emailUser = authService.register(email, password, name);
            folderService.createRootFolders(emailUser);
            String token = ConfirmationToken.createJWT(Long.toString(emailUser.getId()), "docs-app", "activation email", 5 * 1000 * 60);
            String link = Activation.buildLink(token);
            String mail = Activation.buildEmail(emailUser.getName(), link);
            EmailUtil.send(emailUser.getEmail(), mail, "activate account");
            return new Response.Builder()
                    .message("Account has been successfully registered and created!")
                    .statusCode(201)
                    .data(true)
                    .status(HttpStatus.CREATED)
                    .build();
        } catch (MessagingException | IOException e) {
            logger.error("in AuthController -> register -> " + e.getMessage());
            return new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .data(false)
                    .statusCode(400)
                    .message(e.getMessage()).build();
        }
    }

    public Response login(User user) {
        User userInDb = null;
        try {
            userInDb = userService.findByEmail(user.getEmail());
            if (!userInDb.getActivated()) {
                return new Response.Builder()
                        .message(ExceptionMessage.USER_NOT_ACTIVATED.toString())
                        .status(HttpStatus.FORBIDDEN)
                        .statusCode(400)
                        .build();
            }
            String email = user.getEmail();
            String password = user.getPassword();
            Validations.validate(Regex.EMAIL.getRegex(), email);
            Validations.validate(Regex.PASSWORD.getRegex(), password);
            return new Response.Builder()
                    .data(authService.login(email, password))
                    .message("Successfully created a token for a user.")
                    .statusCode(200)
                    .status(HttpStatus.OK)
                    .build();
        } catch (IllegalArgumentException | NullPointerException e) {
            logger.error("in AuthController -> login -> " + e.getMessage());
            return new Response.Builder()
                    .message("You must include all and exact parameters for such an action: email, name, password")
                    .status(HttpStatus.FORBIDDEN)
                    .statusCode(400)
                    .build();
        } catch (AccountNotFoundException e) {
            logger.error("in AuthController -> login -> " + e.getMessage());
            return new Response.Builder()
                    .message("You must include all and exact parameters for such an action: email, name, password")
                    .status(HttpStatus.UNAUTHORIZED)
                    .statusCode(400)
                    .build();
        }
    }

    public Response activate(String token) {
        try {
            String parsedToken = null;
            parsedToken = URLDecoder.decode(token, StandardCharsets.UTF_8.toString()).replaceAll(" ", ".");
            Claims claims = null;
            claims = ConfirmationToken.decodeJWT(parsedToken);
            if (userService.findById(Long.valueOf(claims.getId())).getActivated()) {
                return new Response.Builder()
                        .message("account already activated")
                        .status(HttpStatus.CONFLICT)
                        .statusCode(400)
                        .build();
            }
            authService.activate(Long.valueOf(claims.getId()));
            return new Response.Builder()
                    .message("account activated successfully!")
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .build();
        } catch (ExpiredJwtException e) {
            try {
                String id = e.getClaims().getId();
                User user = userService.findById(Long.valueOf(id));
                EmailUtil.reactivateLink(user);
                return new Response.Builder()
                        .message("the link expired, new activation link has been sent")
                        .statusCode(410)
                        .status(HttpStatus.GONE)
                        .build();
            } catch (AccountNotFoundException ex) {
                logger.error("in AuthController -> activate -> " + e.getMessage());
                return new Response.Builder()
                        .message("activation link expired, failed to send new link")
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .statusCode(500)
                        .build();
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("in AuthController -> activate -> " + e.getMessage());
            return new Response.Builder()
                    .message("failed to activate account")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .statusCode(500)
                    .build();
        } catch (AccountNotFoundException e) {
            logger.error("in AuthController -> activate -> " + e.getMessage());
            return new Response.Builder()
                    .message("invalid token")
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .build();
        }
    }

}
