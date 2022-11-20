package docSharing.service;

import docSharing.entity.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private User user;
    @BeforeEach
    public void createUser(){
        user=User.createUser("test@test.com", "abcd123!", "test");
    }

    @Test
    public void isActivated_False_DefaultUser() {
        assertFalse(user.getActivated(), "user created with activated = true");
    }
    @Test
    public void nameEquals_Test_DefaultUser(){
        assertEquals(user.getName(),"test","name does not equal to inserted name");
    }
    @Test
    public void emailEquals_Test_DefaultUser(){
        assertEquals(user.getEmail(),"test@test.com","email does not equal to inserted email");
    }
    @Test
    public void passwordEquals_Test_DefaultUser(){
        assertEquals(user.getPassword(),"abcd123!","password does not equal to inserted password");
    }
    @Test
    public void setName_NameChanged(){
        user.setName("anotherName");
        assertEquals(user.getName(),"anotherName","name didn't change");
    }
    @Test
    public void setEmail_EmailChanged(){
        user.setEmail("another@test.com");
        assertEquals(user.getEmail(),"another@test.com","email didn't change");
    }
    @Test
    public void setPassword_PasswordChanged(){
        user.setPassword("anotherPass");
        assertEquals(user.getPassword(),"anotherPass","password didn't change");
    }
    @Test
    public void setActivated_ActivatedChanged(){
        user.setActivated(true);
        assertTrue(user.getActivated(),"user is not activated");
    }
}
