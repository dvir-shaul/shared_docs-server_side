package docSharing.service;

import docSharing.Entities.User;
import docSharing.Utils.Activation;
import docSharing.Utils.Activation;
import docSharing.Utils.ExceptionMessage;
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
     *
     * @param email    - mail of user
     * @param password - password of user
     * @param name     - name of user
     */
    public Boolean register(String email, String password, String name) {
        //validate
        Optional<User> checkedUser = userRepository.findByEmail(email);

        // if there is already such a user -> throw error with relevant msg

        // if there is no such an email in the database -> create a new entry for this user in the database

        if (!checkedUser.isPresent()) {
            User user = userRepository.save(User.createUser(email, password, name));
            return true;
        } else if (!checkedUser.get().getActivated() && validate(email, password, name)) {
            return true;
        }
        throw new IllegalArgumentException(ExceptionMessage.DUPLICATED_UNIQUE_FIELD.toString() + email);
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

        if (user.get().getEmail().equals(email) && user.get().getPassword().equals(password)) {
            // generate token
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

    /**
     * validate functions check if the data entry was the same as the database have.
     *
     * @param email    - mail of user.
     * @param password - password of user.
     * @param name     - name of user.
     * @return true if all values are matched to the database.
     */
    private Boolean validate(String email, String password, String name) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() &&
                user.get().getEmail().equals(email) &&
                user.get().getPassword().equals(password) &&
                user.get().getName().equals(name)) {
            return true;
        }
        return false;
    }
}
