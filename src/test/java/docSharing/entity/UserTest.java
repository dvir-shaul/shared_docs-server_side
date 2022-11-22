package docSharing.entity;

import docSharing.entity.User;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class UserTest {


    @Test
    public void isActivated_False_DefaultUser() {
        User user=User.createUser("test@test.com", "abcd123!", "test");
        assertFalse(user.getActivated(), "user created with activated = true");
    }
    @Test
    public void nameEquals_Test_DefaultUser(){
        User user=User.createUser("test@test.com", "abcd123!", "test");
        assertEquals(user.getName(),"test","name does not equal to inserted name");
    }
    @Test
    public void emailEquals_Test_DefaultUser(){
        User user=User.createUser("test@test.com", "abcd123!", "test");
        assertEquals(user.getEmail(),"test@test.com","email does not equal to inserted email");
    }
    @Test
    public void passwordEquals_Test_DefaultUser(){
        User user=User.createUser("test@test.com", "abcd123!", "test");
        assertEquals(user.getPassword(),"abcd123!","password does not equal to inserted password");
    }
    @Test
    public void createUser_nullName_ExceptionThrown(){
        assertThrows(IllegalArgumentException.class,()->User.createUser("test@test.com", "abcd123!", null),"created user with null name");
    }
    @Test
    public void createUser_emptyName_ExceptionThrown(){
        assertThrows(IllegalArgumentException.class,()->User.createUser("test@test.com", "abcd123!", ""),"created user with empty name");
    }
    @Test
    public void createUser_nullEmail_ExceptionThrown(){
        assertThrows(IllegalArgumentException.class,()->User.createUser(null, "abcd123!", "test"),"created user with null email");
    }
    @Test
    public void createUser_emptyEmail_ExceptionThrown(){
        assertThrows(IllegalArgumentException.class,()->User.createUser("", "abcd123!", "test"),"created user with empty email");
    }
    @Test
    public void createUser_nullPassword_ExceptionThrown(){
        assertThrows(IllegalArgumentException.class,()->User.createUser("test@test.com", null, "test"),"created user with null password");
    }
    @Test
    public void createUser_emptyPassword_ExceptionThrown(){
        assertThrows(IllegalArgumentException.class,()->User.createUser("test@test.com", "", "test"),"created user with empty password");
    }
    @Test
    public void setName_NameChanged(){
        User user=User.createUser("test@test.com", "abcd123!", "test");
        user.setName("anotherName");
        assertEquals(user.getName(),"anotherName","name was not changed");
    }
    @Test
    public void setEmail_EmailChanged(){
        User user=User.createUser("test@test.com", "abcd123!", "test");
        user.setEmail("another@test.com");
        assertEquals(user.getEmail(),"another@test.com","email was not changed");
    }
    @Test
    public void setPassword_PasswordChanged(){
        User user=User.createUser("test@test.com", "abcd123!", "test");
        user.setPassword("anotherPass");
        assertEquals(user.getPassword(),"anotherPass","password was not changed");
    }
    @Test
    public void setActivated_ActivatedChanged(){
        User user=User.createUser("test@test.com", "abcd123!", "test");
        user.setActivated(true);
        assertTrue(user.getActivated(),"user is not activated");
    }
}
