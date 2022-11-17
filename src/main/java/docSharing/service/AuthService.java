package docSharing.service;

import docSharing.Entities.User;
import docSharing.Utils.Activation;
import docSharing.email.EmailSender;
import docSharing.repository.UserRepository;
import docSharing.service.token.ConfirmationToken;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    /**
     * register function method is used to register users to the  app with given inputs
     *
     * @param email - mail of user
     * @param password - password of user
     * @param name - name of user
     */
    public Boolean register(String email, String password, String name){
        //validate
        Optional<User> checkedUser = userRepository.findByEmail(email);
        if(! checkedUser.isPresent()){
            User user = userRepository.save(User.createUser(email, password, name));
//            Activation.sendActivationEmail(user);
            return true;
        } else if (! checkedUser.get().getActivated() && validate(email,password,name)) {
//          Activation.sendActivationEmail(checkedUser.get());
            return true;
        }
        // throw execption Error Code: 1062. Duplicate entry 'dvir@gmail.com' for key 'user.UK_ob8kqyqqgmefl0aco34akdtpe'
        System.out.println("User already exists with activated value");
        return false;
    }

    /**
     * login to app and check if inputs was correct according to database
     * @param email - mail of user
     * @param password - password
     * @return token for user to be unique on app
     */
    public String login(String email, String password){
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && user.get().getEmail().equals(email) && user.get().getPassword().equals(password)){
            // generate token
             return "token";
        }
        return "not token";
    }

    /**
     * activate function meant to change the user column isActivated from originated value false to true.
     * Repository go to the unique row that has user email and changed that value.
     * Method used after a user clicks on the link he got on email.
     * @param email - user email
     */
    private void activate(String email){
        userRepository.updateIsActivated(true,email);
    }

    private Boolean validate(String email, String password, String name){
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
