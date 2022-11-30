package docSharing.controller;

import docSharing.entity.Folder;
import docSharing.entity.GeneralItem;
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
class AbstractControllerTest {

    @Autowired
    AbstractController abstractController;

    @Test
    void getAll() {
        assertNotNull(abstractController);
    }

    @Test
    void create() {
    }

    @Test
    void rename() {
    }

    @Test
    void delete() {
    }

    @Test
    void relocate() {
    }
}