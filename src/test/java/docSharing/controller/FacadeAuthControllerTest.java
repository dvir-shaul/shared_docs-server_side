package docSharing.controller;

import com.google.api.services.gmail.Gmail;
import docSharing.entity.User;
import docSharing.repository.UserRepository;
import docSharing.service.AuthService;
import docSharing.service.FolderService;
import docSharing.service.UserService;
import docSharing.utils.EmailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import javax.security.auth.login.AccountNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class FacadeAuthControllerTest {
    @Mock
    private AuthService authService;
    @Mock
    private UserService userService;
    @Mock
    private FolderService folderService;
    @Mock
    private static Gmail service;

    @InjectMocks
    private EmailUtil emailUtil;
    @InjectMocks
    private FacadeAuthController facadeAuthController;

    private User goodUser;
    private User badEmailUser;
    private User badPasswordUser;
    private User badNameUser;

    @BeforeEach
    @DisplayName("Make sure we have all necessary items, good as new, for each test when it's called")
    void setUp() {
        goodUser = User.createUser("asaf396@gmai.com", "dvir1234", "dvir");
        goodUser.setId(1l);
        goodUser.setActivated(true);

        badEmailUser = User.createUser("Dvgmai.com", "dvir1234567", "dvir");
        badPasswordUser = User.createUser("Dvgmai@gmail.com", "4", "dviros");
        badNameUser = User.createUser("Dvgmai@gmail.com", "dvir1234567", "1");
    }

    @Test
    @DisplayName("When sending all the correct parameters, successfully register the user to the database")
    void register_goodUser_Successfully() throws AccountNotFoundException {
        given(authService.register(goodUser.getEmail(), goodUser.getPassword(), goodUser.getName())).willReturn(goodUser);
        doNothing().when(folderService).createRootFolders(goodUser);
        assertEquals(201, facadeAuthController.register(goodUser).getStatusCode(), "register with good user parameters did not return 201");
    }

    @Test
    @DisplayName("Given a wrong email address, expect a bad response to be returned")
    void register_badUserEmail_BAD_REQUEST() {
        assertEquals(400, facadeAuthController.register(badEmailUser).getStatusCode(), "register with bad email user parameters did not return 400");
    }

    @Test
    @DisplayName("Given a wrong password input, expect a bad response to be returned")
    void register_badUserPassword_BAD_REQUEST() {
        assertEquals(400, facadeAuthController.register(badPasswordUser).getStatusCode(), "register with bad password user parameters did not return 400");
    }

    @Test
    @DisplayName("Given a wrong name input, expect a bad response to be returned")
    void register_badUserName_BAD_REQUEST() {
        assertEquals(400, facadeAuthController.register(badNameUser).getStatusCode(), "register with bad name user parameters did not return 400");
    }

    @Test
    @DisplayName("Given an email address that already exists in the database, when trying to register, expect a bad response to be returned")
    void register_withSameEmailAgain_BAD_REQUEST() {
        assertEquals(400, facadeAuthController.register(goodUser).getStatusCode(), "register with good user parameters did not return 400");
    }

    @Test
    @DisplayName("When trying to log in with all the correct parameters, successfully return a positive response")
    void login_goodUser_success() throws AccountNotFoundException {
        given(userService.findByEmail(goodUser.getEmail())).willReturn(goodUser);
        when(authService.login(goodUser.getEmail(), goodUser.getPassword())).thenReturn("token");
        assertEquals(200, facadeAuthController.login(goodUser).getStatusCode(), "Login with correct input did not return positive response");
    }

    @Test
    @DisplayName("When given the wrong password input, after an account with a different password, return negative response")
    void login_failWrongPassword_FORBIDDEN() throws AccountNotFoundException {
        given(userService.findByEmail(goodUser.getEmail())).willReturn(goodUser);
        when(authService.login(goodUser.getEmail(), goodUser.getPassword())).thenThrow(new IllegalArgumentException("test to login with a wrong password"));
        assertEquals(400, facadeAuthController.login(goodUser).getStatusCode(), "test to login with a wrong password did not return errorCode 400");
    }

    @Test
    @DisplayName("When given an unregistered account's email, return a negatie response")
    void login_noEmailInDB_UNAUTHORIZED() throws AccountNotFoundException {
        given(userService.findByEmail(goodUser.getEmail())).willThrow(new AccountNotFoundException("No existing account in the db"));
        assertEquals(401, facadeAuthController.login(goodUser).getStatusCode(), "test to login with a non-existing account did not return errorCode 401");
    }

    @Test
    @DisplayName("When given a wrong token string, expect to throw an exception")
    void activate_wrongToken_throwException() {
        assertEquals(400, facadeAuthController.activate("shalom lah geveret Dvir").getStatusCode(), "Activation a user with wrong token did not throw exception");
    }
}