package docSharing.controller;

import docSharing.entity.User;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthControllerTest {
    //register
    //login
    //activate
    @Autowired
    AuthController authController;
//    @Autowired
//    UserController userController;
//    @Autowired
//    UserService userService;
//    @Autowired
//    private static UserRepository userRepository;
//
//    private Map<String, SubmitedUser> useresCode = new HashMap<>();
//    private Map<String, User> registeredUsers = new HashMap<>();
//    private Map<Integer, String> tokens = new HashMap<>();
//
//
//    @BeforeEach
//    public void setup() throws NoSuchAlgorithmException {
//        SubmitedUser user = new SubmitedUser("saraysara1996@gmail.com", "123456", "sara");
//        User myUser = new User.Builder(user.getEmail(), ValidationUtils.secretPassword(user.getPassword()), user.getNickName()).build();
//    }
//
//    @Test
//    public void createUser_Successfully() throws SQLDataException {
//        SubmitedUser user = new SubmitedUser("gffrvtcvrfvb@gmail.com", "123456", "sara");
//        ResponseEntity<String> r = userController.createUser(user);
//        assertEquals(200, r.getStatusCodeValue());
//        System.out.println(r.getStatusCode());
//    }

    @Test
    public void createUser_Returns201() {
        User user = User.createUser("test@test.com", "abcd1234", "test");
        ResponseEntity<String> response = authController.register(user);
        assertEquals(201, response.getStatusCodeValue());
    }
}
