package docSharing.service;

import static org.assertj.core.api.Assertions.assertThat;

import docSharing.entity.User;
import docSharing.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


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
        given(userRepository.findByEmail(user.getEmail())).willReturn(null);
        given(userRepository.save(user)).willReturn(user);
        User savedUser = authService.register(user.getEmail(), user.getPassword(), user.getName());
        assertThat(savedUser).isNotNull(); // FIXME: more than not null, it should return the same user
    }

    @Test
    @DisplayName("??")
    public void givenExistingEmail_whenRegisterUser_thenThrowsException() {
       given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.ofNullable(user));
        Assertions.assertThrows(RuntimeException.class, () -> {
            authService.register(user.getEmail(), user.getPassword(), user.getName());
        });
    }

    @Test
    @DisplayName("Get back a token once any user logs in")
    public void givenExistingEmail_whenLoginUser_thenReturnsToken() {
        try {
//            given(userRepository.findByEmail(user.getEmail()).get()).willReturn(user);
            String token = authService.login(user.getEmail(), user.getPassword());
            assertThat(token).isNotNull();
        } catch (AccountNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Check if authService throws exception when a user logs in with the wrong email")
    public void givenNotExistingEmail_whenLoginUser_thenThrowsException() {
        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.ofNullable(user));
        Assertions.assertThrows(AccountNotFoundException.class, () -> {
            authService.login(user.getEmail(), user.getPassword());
        });
    }

    @Test
    @DisplayName("Make sure a user is being activated after accepted the activation link")
    public void givenUserId_whenActivatingUser_thenActivateUser() {
        given(userRepository.updateIsActivated(true, user.getId())).willReturn(1);
        assertThat(authService.activate(user.getId())).isEqualTo(1);
    }
}
