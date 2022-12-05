package docSharing.service;//package docSharing.service;

import docSharing.entity.User;
import docSharing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.security.auth.login.AccountNotFoundException;

import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private AuthService authService;
    private User user;

    @BeforeEach
    public void createUser() {
        user = User.createUser("test@test.com", "abcd123", "test user");
    }


    @Test
    @DisplayName("Make sure when we register a user, it adds it to the database correctly")
    public void givenUserObject_whenSaveUser_thenReturnUserObject() {
        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.empty());
        given(userRepository.save(user)).willReturn(user);
        User savedUser = authService.register(user.getEmail(), user.getPassword(), user.getName());
        assertEquals(savedUser, user);
    }


    @Test
    @DisplayName("??")
    public void givenExistingEmail_whenRegisterUser_thenThrowsException() {
        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class, () -> {
            authService.register(user.getEmail(), user.getPassword(), user.getName());
        });
    }

    @Test
    @DisplayName("Make sure a user is being activated after accepted the activation link")
    public void givenUser_whenRegisterUser_thenUserNotActivated() {
        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.empty());
        given(userRepository.save(user)).willReturn(user);
        User savedUser = authService.register(user.getEmail(), user.getPassword(), user.getName());
        assertEquals(savedUser.getActivated(), false);
    }

    @Test
    @DisplayName("Get back a token once any user logs in")
    public void givenExistingEmail_whenLoginUser_thenLogin() {
        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        assertDoesNotThrow(() -> authService.login(user.getEmail(), user.getPassword()));
    }

    @Test
    @DisplayName("Check if authService throws exception when a user logs in with the wrong email")
    public void givenNotExistingEmail_whenLoginUser_thenThrowsException() {
        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> {
            authService.login(user.getEmail(), user.getPassword());
        });
    }

    @Test
    @DisplayName("Check if authService throws exception when a user logs in with the wrong password")
    public void givenWrongPassword_whenLoginUser_thenThrowsException() {
        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class, () -> {
            authService.login(user.getEmail(), "qwer1234");
        });
    }

    @Test
    @DisplayName("Make sure a user is being activated after accepted the activation link")
    public void givenUserId_whenActivatingUser_thenActivateUser() {
        given(userRepository.updateIsActivated(true, user.getId())).willReturn(1);
        when(authService.activate(user.getId())).thenReturn(1);
        assertEquals(1, authService.activate(user.getId()));
    }

    @Test
    @DisplayName("given invalid token throw exception")
    public void givenInvalidToken_whenValidatingToken_thenThrowIllegalException() {
        assertThrows(IllegalArgumentException.class, () -> authService.checkTokenToUserInDB("invalidToken"), "invalid token did not throw illegal argument exception");
    }
    @Test
    @DisplayName("given null token throw exception")
    public void givenNullToken_whenValidatingToken_thenThrowIllegalException() {
        assertThrows(NullPointerException.class, () -> authService.checkTokenToUserInDB(null), "null token did not throw illegal argument exception");
    }
    @Test
    @DisplayName("given valid token of user that does not exist")
    public void givenTokenOfUserThatNotExists_whenValidatingToken_thenThrowAccountNotFoundException() {
        assertThrows(AccountNotFoundException.class, () -> authService.checkTokenToUserInDB("Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxIiwiaWF0IjoxNjY5OTAzOTgwLCJzdWIiOiJsb2dpbiIsImlzcyI6ImRvY3MgYXBwIn0.83vs6fa8rGm6kyrZz4K8YYBcIO2N0aBu_lQz-QPFaus"
        ), "valid token of user that does not exist did not throw account not found exception");
    }
}
