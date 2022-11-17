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

    private UserRepository userRepository;
private EmailSender emailSender;
    public void register(String email, String password, String name) {

        // check if this email already exisits in the database
        // Boolean doesUserEmailExist = userRepository.findByEmail(email).isPresent();

        // create new user with the email, password, name
//         userRepository.save(user.get());
        // send activation email
        User user = User.createUser(name, password, email);
        String token = ConfirmationToken.createJWT(Integer.toString(user.getId()), "docs-app", "activation email", 5000);
        String link = Activation.buildLink(token);
        String mail=Activation.buildEmail(user.getName(), link);
        emailSender.send(user.getEmail(), mail);
        // check if activated ?
        // if activated -> user already exists

        // if inactivated -> resend email -> send activation email

        // if correct -> send data to user repo

    }

    public void login(String email, String password) {
        // Optional<User> user = userRepository.findByEmail(email);
        // check if email == email etc...
        // generate token
        // return token
    }

    public void activate(Long id) {
        // check if this email already exisits in the database
        // Optional<User> user = userRepository.findById(id);
        // userRepository.setIsActivated(true, id);

    }

    private void validate(String email, String password, String name) {

        // validate the data if needed
    }
}
