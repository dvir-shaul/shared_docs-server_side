package docSharing.service;

import docSharing.entity.User;
import docSharing.utils.ExceptionMessage;
import docSharing.repository.UserRepository;
import docSharing.service.token.ConfirmationToken;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Optional;

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
        Optional<User> checkedUser = userRepository.findByEmail(email);
        if (checkedUser.isPresent()) throw new IllegalArgumentException(ExceptionMessage.DUPLICATED_UNIQUE_FIELD.toString() + email);
        return userRepository.save(User.createUser(email, password, name));
    }

    /**
     * login to app and check if inputs was correct according to database
     * @param email    - mail of user
     * @param password - password
     * @return token for user to be unique on app
     */
    public String login(String email, String password) throws AccountNotFoundException {
        Optional<User> user = userRepository.findByEmail(email);
        if(!user.isPresent()) throw new AccountNotFoundException(ExceptionMessage.NO_ACCOUNT_IN_DATABASE.toString());

        if (user.get().getPassword().equals(password)) {
            return ConfirmationToken.createJWT(String.valueOf(user.get().getId()), "docs app", "login", 5*1000);
        }
        throw new IllegalArgumentException(ExceptionMessage.NOT_MATCH.toString());
    }

    /**
     * activate function meant to change the user column isActivated from originated value false to true.
     * Repository go to the unique row that has user email and changed that value.
     * Method used after a user clicks on the link he got on email.
     *
     * @param id - user email
     */
    public void activate(Long id) {
        // check if id exists
        userRepository.updateIsActivated(true, id);
    }
}
