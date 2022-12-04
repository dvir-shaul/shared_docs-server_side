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
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals(authController.register(goodUser).getStatusCode().toString().substring(0,3),"200");
    }
    @Test
    void register_goodUserAndThenAgainDifferentMail_Successfully() {
        assertEquals(authController.register(goodUser).getStatusCode().toString().substring(0,3),"200");
        goodUser.setEmail("dvir@gmail.com");
        assertEquals(authController.register(goodUser).getStatusCode().toString().substring(0,3),"200");
    }
    @Test
    void register_badUserEmail_BAD_REQUEST() {
        assertEquals(authController.register(badUser).toString().substring(1,4),
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionMessage.VALIDATION_FAILED+badUser.getEmail()).toString().substring(1,4));

    }
    @Test
    void register_badUserPassword_BAD_REQUEST() {
        badUser=User.createUser(goodUser.getEmail(),"1","dvir");
        assertEquals(authController.register(badUser).toString().substring(1,4),
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionMessage.VALIDATION_FAILED+badUser.getPassword()).toString().substring(1,4));

    }

    @Test
    void register_withSameEMailAgain_BAD_REQUEST(){
        assertEquals(authController.register(goodUser).getStatusCode().toString().substring(0,3),"200");
        assertEquals(authController.register(goodUser).toString().substring(1,4),
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This user email already exists: "+goodUser.getEmail()).toString().substring(1,4));;
    }

    @Test
    void login_goodUser_success() throws AccountNotFoundException {
        User user = User.createUser("asaf3964@gmail.com","dvir1234","dvir");
        authController.register(user);
        Long id = userRepository.findByEmail(user.getEmail()).get().getId();
        user.setId(id);
        String token = ConfirmationToken.createJWT(Long.toString(id), "docs-app", "activation email", 5*1000*60);
        assertEquals(authController.activate(token).getStatusCode().toString().substring(0,3),"200");
        User loginUser = User.createUserForLoginTest("asaf3964@gmail.com","dvir1234");
        assertEquals(authController.login(loginUser).getStatusCode().toString().substring(0,3),"200");//return token..

    }
    @Test
    void login_failWrongPassword_FORBIDDEN() throws AccountNotFoundException {
        User user = User.createUser("asaf3964@gmail.com","dvir1234","dvir");
        authController.register(user);
        Long id = userRepository.findByEmail(user.getEmail()).get().getId();
        user.setId(id);
        String token = ConfirmationToken.createJWT(Long.toString(id), "docs-app", "activation email", 5*1000*60);
        assertEquals(authController.activate(token).getStatusCode().toString().substring(0,3),"200");
        User loginUser = User.createUserForLoginTest("asaf3964@gmail.com","dvir12");
        assertEquals(authController.login(loginUser).toString().substring(1,4),
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(ExceptionMessage.VALIDATION_FAILED+loginUser.getPassword()).toString().substring(1,4));
    }
    @Test
    void login_noEmailInDB_UNAUTHORIZED(){
        User loginUser = User.createUserForLoginTest("asaf3964@gmail.com","dvir1223");
        assertEquals(authController.login(loginUser).toString().substring(1,4),
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(ExceptionMessage.NO_ACCOUNT_IN_DATABASE + loginUser.getEmail()).toString().substring(1,4));
    }

    @Test
    void activate_goodUser_success() throws AccountNotFoundException {
        User user = User.createUser("asaf3964446@gmail.com","dvir1234","dvir");
        authController.register(user);
        Long id = userRepository.findByEmail(user.getEmail()).get().getId();
        String token = ConfirmationToken.createJWT(Long.toString(id), "docs-app", "activation email", 5*1000*60);
        assertEquals(authController.activate(token).getStatusCode().toString().substring(0,3),"200");
    }
    @Test
    void activate_wrongToken_throwException() throws AccountNotFoundException {
        User user = User.createUser("asaf3964446@gmail.com","dvir1234","dvir");
        authController.register(user);
        Long id = userRepository.findByEmail(user.getEmail()).get().getId();
        String token = ConfirmationToken.createJWT(Long.toString(id+1), "docs-app", "activation email", 5*1000*60);
        assertEquals(authController.activate(token).toString().substring(1,4),"400");
    }
//    @Test
//    void createUser_Returns201() {
//        User user = User.createUser("test@test.com", "abcd1234", "test");
//        ResponseEntity<String> response = authController.register(user);
//        assertEquals(201, response.getStatusCodeValue());
//    }
}