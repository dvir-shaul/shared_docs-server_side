package docSharing.service;

import docSharing.entity.User;
import docSharing.utils.ExceptionMessage;
import docSharing.repository.UserRepository;
import docSharing.utils.ConfirmationToken;
import docSharing.utils.Validations;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthService {
    private static Logger logger = LogManager.getLogger(AuthService.class.getName());

    @Autowired
    private UserRepository userRepository;

    /**
     * register   method is used to register new users to the app with given inputs
     *
     * @param email    - mail of user
     * @param password - password of user
     * @param name     - name of user
     * @return - entity of the user we just register.
     */
    public User register(String email, String password, String name) {
        logger.info("in AuthService -> register");
        if (userRepository.findByEmail(email).isPresent()) {
            logger.error("in AuthService -> register -> fail: " + ExceptionMessage.ACCOUNT_EXISTS + email);
            throw new IllegalArgumentException(ExceptionMessage.ACCOUNT_EXISTS + email);
        }
        return userRepository.save(User.createUser(email, password, name));
    }

    /**
     * login function method is used to log-in users to the app and check if inputs was correct according to database.
     * first check if we have the email in the database and then proceed to generate token.
     *
     * @param email    - mail of user
     * @param password - password
     * @return - token for user to be unique on app
     */
    public String login(String email, String password) throws AccountNotFoundException {
        logger.info("in AuthService -> login");
        Optional<User> user = userRepository.findByEmail(email);
        if (!user.isPresent()) {
            logger.error("in AuthService -> login -> fail: " + ExceptionMessage.NO_ACCOUNT_IN_DATABASE + email);
            throw new AccountNotFoundException(ExceptionMessage.NO_ACCOUNT_IN_DATABASE + email);
        }

        if (!user.get().getPassword().equals(password)) {
            logger.error("in AuthService -> login -> fail: " + ExceptionMessage.NOT_MATCH);
            throw new IllegalArgumentException(ExceptionMessage.NOT_MATCH.toString());
        }

        return generateToken(user.get());
    }

    /**
     * activate function meant to change the user column isActivated from originated value false to true.
     * Repository go to the unique row that has user email and changed that value.
     * Method used after a user clicks on the link he got on email.
     *
     * @param id - user email
     * @return - should be always 1, which is rows affected in the database.
     */
    public int activate(Long id) {
        logger.info("in AuthService -> activate");
        return userRepository.updateIsActivated(true, id);
    }

    /**
     * generateToken is a function that creates a unique JWT token for every logged-in user.
     *
     * @param user - user
     * @return generated token according to: io.jsonwebtoken.Jwts library
     */
    private String generateToken(User user) {
        return ConfirmationToken.createJWT(String.valueOf(user.getId()), "docs app", "login", 0);
    }

    /**
     * called by functions to check if the token is a valid user token
     * and checks if we have the user id we got from the Validations.validateToken(token) in the database.
     *
     * @return - id of user
     */
    public Long checkTokenToUserInDB(String token) throws AccountNotFoundException {
        logger.info("in AuthService -> isValid");
        long id = Validations.validateToken(token);
        if (!userRepository.existsById(id)) {
            logger.error("in AuthService -> isValid ->" + ExceptionMessage.NO_USER_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        }

        return id;
    }
}
