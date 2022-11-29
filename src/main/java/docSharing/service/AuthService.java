package docSharing.service;

import docSharing.entity.User;
import docSharing.utils.ExceptionMessage;
import docSharing.repository.UserRepository;
import docSharing.utils.ConfirmationToken;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.security.auth.login.AccountNotFoundException;

@Service
@AllArgsConstructor
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    /**
     * register function method is used to register users to the  app with given inputs
     * @param email    - mail of user
     * @param password - password of user
     * @param name     - name of user
     */
    public User register(String email, String password, String name) {
        if (! userRepository.findByEmail(email).isPresent())
            throw new IllegalArgumentException(ExceptionMessage.ACCOUNT_EXISTS + email);
        return userRepository.save(User.createUser(email, password, name));
    }

    /**
     * login to app and check if inputs was correct according to database
     * @param email    - mail of user
     * @param password - password
     * @return token for user to be unique on app
     */
    public String login(String email, String password) throws AccountNotFoundException {
        if (! userRepository.findByEmail(email).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_ACCOUNT_IN_DATABASE + email);
        User user = userRepository.findByEmail(email).get();
        if (userRepository.findByEmail(email).get().getPassword().equals(password)) {
            return generateToken(user);
        }
        throw new IllegalArgumentException(ExceptionMessage.NOT_MATCH.toString());
    }

    /**
     * activate function meant to change the user column isActivated from originated value false to true.
     * Repository go to the unique row that has user email and changed that value.
     * Method used after a user clicks on the link he got on email.
     * @param id - user email
     */
    public int activate(Long id) {
        // check if id exists
        return userRepository.updateIsActivated(true, id);
    }

    /**
     * @param user - user
     * @return generated token according to: io.jsonwebtoken.Jwts library
     */
    private String generateToken(User user) {
        return ConfirmationToken.createJWT(String.valueOf(user.getId()), "docs app", "login", 0);
    }

}
