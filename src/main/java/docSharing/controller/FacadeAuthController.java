package docSharing.controller;

import docSharing.entity.User;
import docSharing.response.Response;
import docSharing.service.AuthService;
import docSharing.service.FolderService;
import docSharing.service.UserService;
import docSharing.utils.*;
import io.jsonwebtoken.*;
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

    /**
     * Register function is responsible for creating new users and adding them to the database.
     * Users will use their personal information to create a new account: email, password, name.
     *
     * @param user - User with email, name and password
     * @return Response with status 201 if good or 400 if something went wrong.
     */
    public Response register(User user) {
        logger.info("in FacadeAuthController -> register");
        // make sure we got all the data from the client

        if (Validations.validateWrongInputRegister(user)) {
            logger.error("in FacadeAuthController -> register -> one of email, name, password is null");
            return new Response.Builder()
                    .message("You must include all and exact parameters for such an action: email, name, password")
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .build();
        }
        try {
            String email = user.getEmail();
            String name = user.getName();
            String password = user.getPassword();

            Validations.validate(Regex.NAME.getRegex(), name);
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
        } catch (MessagingException | IllegalArgumentException | NullPointerException | IOException e) {
            logger.error("in FacadeAuthController -> register -> " + e.getMessage());
            return new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .data(false)
                    .statusCode(400)
                    .message(e.getMessage()).build();
        }
    }

    /**
     * Login function is responsible for logging user into the system.
     * This function accepts only 2 parameters: email, password.
     * If the credentials match to the database's information, it will allow the user to use its functionalities.
     * A token will be returned in a successful request.
     *
     * @param user - user's details with email and password to check if correct
     * @return Response with user's token and status 200 if good or 400 if something went wrong.
     */
    public Response login(User user) {
        logger.info("in FacadeAuthController -> login");

        User userInDb = null;
        try {
            userInDb = userService.findByEmail(user.getEmail());
            if (!userInDb.getActivated()) {
                logger.error("in FacadeAuthController -> login -> user email:" + user.getEmail() + " ," + ExceptionMessage.USER_NOT_ACTIVATED);
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
            logger.error("in FacadeAuthController -> login -> " + e.getMessage());
            return new Response.Builder()
                    .message("You must include all and exact parameters for such an action: email, name, password")
                    .status(HttpStatus.FORBIDDEN)
                    .statusCode(400)
                    .build();
        } catch (AccountNotFoundException e) {
            logger.error("in FacadeAuthController -> login -> AccountNotFoundException-> " + e.getMessage());
            return new Response.Builder()
                    .message("You must include all and exact parameters for such an action: email, name, password")
                    .status(HttpStatus.UNAUTHORIZED)
                    .statusCode(401)
                    .build();
        }
    }

    /**
     * Activate function is responsible for activating email links.
     * If the link is not expired, make the user activated in the database.
     * If the link is expired, resend a new link to the user with a new token.
     *
     * @param token - A link with activation token
     * @return Response with data and status 200 if good or 400 if something went wrong.
     */
    public Response activate(String token) {
        logger.info("in FacadeAuthController -> activate");
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
                logger.error("in FacadeAuthController -> activate ->AccountNotFoundException-> " + e.getMessage());
                return new Response.Builder()
                        .message("activation link expired, failed to send new link")
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .statusCode(500)
                        .build();
            }

        } catch (UnsupportedEncodingException | UnsupportedJwtException | MalformedJwtException | SignatureException |
                 IllegalArgumentException e) {
            logger.error("in FacadeAuthController -> activate -> " + e.getMessage());
            return new Response.Builder()
                    .message("failed to activate account")
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .build();
        } catch (AccountNotFoundException e) {
            logger.error("in FacadeAuthController -> activate -> AccountNotFoundException ->" + e.getMessage());
            return new Response.Builder()
                    .message("invalid token")
                    .status(HttpStatus.FORBIDDEN)
                    .statusCode(403)
                    .build();
        }
    }

}
