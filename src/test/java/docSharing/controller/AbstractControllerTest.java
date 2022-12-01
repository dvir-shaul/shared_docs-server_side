package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Folder;
import docSharing.entity.GeneralItem;
import docSharing.entity.User;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserRepository;
import docSharing.response.FileRes;
import docSharing.utils.ConfirmationToken;
import docSharing.utils.ExceptionMessage;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeAll;
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

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)@SpringBootTest
class AbstractControllerTest {

    @Autowired
    AbstractController abstractController;

    @Autowired
    UserRepository userRepository;
    @Autowired
    FolderRepository folderRepository;
    @Autowired
    DocumentRepository documentRepository;
    @BeforeEach
    void setup(){
        userRepository.deleteAll();
        folderRepository.deleteAll();
        User user = userRepository.save(User.createUser("testUser@gmail.com", "2222222","tester"));
        long userId = userRepository.findByEmail("testUser@gmail.com").get().getId();
        userRepository.updateIsActivated(true,userId);
        Folder folder = new Folder();
        folder.setName("folder");
        folder.setUser(user);
        Document document = Document.createDocument(user,"newDoc",folder,"hey");
        folder.addDocument(document);
        folderRepository.save(folder);
        documentRepository.save(document);

    }

    @Test
    void notNull(){
        assertNotNull(abstractController);
    }

    @Test
    void getAll() {
        ResponseEntity<List<FileRes>> list = abstractController.getAll(null,userRepository.findByEmail("testUser@gmail.com").get().getId() );
        assertFalse(Objects.requireNonNull(list.getBody()).isEmpty());
    }

    @Test
    void getAll_noParentFolderInDB_successfully() throws AccountNotFoundException {
        assertThrows(RuntimeException.class,()->abstractController.getAll(240000L,userRepository.findByEmail("testUser@gmail.com").get().getId()));
    }
    @Test
    void getAll_noUserInDB_RuntimeException() throws AccountNotFoundException {
        assertThrows(RuntimeException.class,()->abstractController.getAll(null,userRepository.findByEmail("tes213tUser@gmail.com").get().getId()));
    }
    @Test
    void createFolder_badInputFolder_NullPointerException() {
        assertThrows(NullPointerException.class,()->abstractController.create(folderRepository.findByNameAndUser("folderrrrrrrrrrrrr",userRepository.findByEmail("testUser@gmail.com").get())
                ,folderRepository.findByNameAndUser("folderrrrrrrrrrrrr",userRepository.findByEmail("testUser@gmail.com").get()).getClass()),"bad folder name input dont throw IllegalArgumentException");
    }

    @Test
    void delete() {
        //LazyInitializationException caused by oneToMany
    }

    @Test
    void relocate() {
    }

}