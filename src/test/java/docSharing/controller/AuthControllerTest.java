package docSharing.controller;

import docSharing.entity.User;
import docSharing.repository.UserRepository;
import docSharing.utils.ConfirmationToken;
import docSharing.utils.ExceptionMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.security.auth.login.AccountNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)@SpringBootTest
class AuthControllerTest {

    @Autowired
    AuthController authController;
    @Autowired
    UserRepository userRepository;

    private User goodUser;
    private User badUser;



    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        goodUser=User.createUser("asaf396@gmai.com","dvir1234","dvir");
        badUser=User.createUser("Dvgmai.com","dvir1234","dvir");
    }

    @Test
    void register_goodUser_Successfully() {
        assertEquals(authController.register(goodUser), ResponseEntity.status(201).body("Account has been successfully registered and created!"));
    }
    @Test
    void register_badUserEmail_BAD_REQUEST() {
        assertEquals(authController.register(badUser), ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionMessage.VALIDATION_FAILED+badUser.getEmail()));

    }
    @Test
    void register_badUserPassword_BAD_REQUEST() {
        badUser=User.createUser(goodUser.getEmail(),"1","dvir");
        assertEquals(authController.register(badUser), ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionMessage.VALIDATION_FAILED+badUser.getPassword()));

    }

    @Test
    void register_withSameEMailAgain_BAD_REQUEST(){
        assertEquals(authController.register(goodUser), ResponseEntity.status(201).body("Account has been successfully registered and created!"));
        assertEquals(authController.register(goodUser), ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This user email already exists: "+goodUser.getEmail()));;
    }

    @Test
    void login_goodUser_success() throws AccountNotFoundException {
        User user = User.createUser("asaf3964@gmail.com","dvir1234","dvir");
        authController.register(user);
        Long id = userRepository.findByEmail(user.getEmail()).get().getId();
        user.setId(id);
        String token = ConfirmationToken.createJWT(Long.toString(id), "docs-app", "activation email", 5*1000*60);
        assertEquals(authController.activate(token),ResponseEntity.status(200).body("account activated successfully!"));
        User loginUser = User.createUserForLoginTest("asaf3964@gmail.com","dvir1234");
        assertEquals(authController.login(loginUser).toString().substring(1,4),"200");//return token..

    }
    @Test
    void login_failWrongPassword_FORBIDDEN() throws AccountNotFoundException {
        User user = User.createUser("asaf3964@gmail.com","dvir1234","dvir");
        authController.register(user);
        Long id = userRepository.findByEmail(user.getEmail()).get().getId();
        user.setId(id);
        String token = ConfirmationToken.createJWT(Long.toString(id), "docs-app", "activation email", 5*1000*60);
        assertEquals(authController.activate(token),ResponseEntity.status(200).body("account activated successfully!"));
        User loginUser = User.createUserForLoginTest("asaf3964@gmail.com","dvir12");
        assertEquals(authController.login(loginUser),ResponseEntity.status(HttpStatus.FORBIDDEN).body(ExceptionMessage.VALIDATION_FAILED+loginUser.getPassword()));
    }
    @Test
    void login_noEmailInDB_UNAUTHORIZED(){
        User loginUser = User.createUserForLoginTest("asaf3964@gmail.com","dvir1223");
        assertEquals(authController.login(loginUser),ResponseEntity.status(HttpStatus.FORBIDDEN).body(ExceptionMessage.NO_ACCOUNT_IN_DATABASE + loginUser.getEmail()));
    }

    @Test
    void activate_goodUser_success() throws AccountNotFoundException {
        User user = User.createUser("asaf3964446@gmail.com","dvir1234","dvir");
        authController.register(user);
        Long id = userRepository.findByEmail(user.getEmail()).get().getId();
        String token = ConfirmationToken.createJWT(Long.toString(id), "docs-app", "activation email", 5*1000*60);
        assertEquals(authController.activate(token),ResponseEntity.status(200).body("account activated successfully!"));
    }
    @Test
    void activate_wrongToken_throwException(){
        User user = User.createUser("asaf3964446@gmail.com","dvir1234","dvir");
        authController.register(user);
        Long id = userRepository.findByEmail(user.getEmail()).get().getId();
        String token = ConfirmationToken.createJWT(Long.toString(id+1), "docs-app", "activation email", 5*1000*60);
        assertThrows(RuntimeException.class, ()->authController.activate(token));
    }
    @Test
    public void createUser_Returns201() {
        User user = User.createUser("test@test.com", "abcd1234", "test");
        ResponseEntity<String> response = authController.register(user);
        assertEquals(201, response.getStatusCodeValue());
    }
}