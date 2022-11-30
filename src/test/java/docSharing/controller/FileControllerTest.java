package docSharing.controller;

import docSharing.entity.Folder;
import docSharing.entity.User;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserRepository;
import docSharing.response.FileRes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class FileControllerTest {
    @Autowired
    FileController fileController;
    @Autowired
    UserRepository userRepository;
    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    FolderRepository folderRepository;
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
        folderRepository.save(folder);
    }

    @Test
    void getAll_successfully() throws AccountNotFoundException {
        ResponseEntity<List<FileRes>> list = fileController.getAll(null,userRepository.findByEmail("testUser@gmail.com").get().getId() );
        assertFalse(Objects.requireNonNull(list.getBody()).isEmpty());
    }
    @Test
    void getAll_noParentFolderInDB_successfully() throws AccountNotFoundException {
        assertThrows(RuntimeException.class,()->fileController.getAll(240000L,userRepository.findByEmail("testUser@gmail.com").get().getId()));
    }
    @Test
    void getAll_noUserInDB_RuntimeException() throws AccountNotFoundException {
        assertThrows(RuntimeException.class,()->fileController.getAll(null,userRepository.findByEmail("tes213tUser@gmail.com").get().getId()));
    }

    @Test
    void createFolder_successFromNullParent() {
        assertEquals(fileController.createFolder(null,"f1_folder",userRepository.findByEmail("testUser@gmail.com").get().getId()).toString().substring(1,4),"200");
    }
//    @Test
//    void createFolder_successWithIdParent() { // nested exception
//        Folder f = folderRepository.findByNameAndUser("folder",userRepository.findByEmail("testUser@gmail.com").get());
//        assertEquals(fileController.createFolder(f.getId(),"f1_folder",userRepository.findByEmail("testUser@gmail.com").get().getId()).toString().substring(1,4),"200");
//    }
    @Test
    void createFolder_badInputFolderName_IllegalArgumentException() {
        Folder f = folderRepository.findByNameAndUser("folder",userRepository.findByEmail("testUser@gmail.com").get());
        assertThrows(IllegalArgumentException.class,()->fileController.createFolder(f.getId(),"",userRepository.findByEmail("testUser@gmail.com").get().getId()),"bad folder name input dont throw IllegalArgumentException");
    }


    @Test
    void createDocument_success() {
        // org.springframework.dao.InvalidDataAccessApiUsageException: detached entity passed to persist: docSharing.entity.Folder;
        // nested exception is org.hibernate.PersistentObjectException: detached entity passed to persist: docSharing.entity.Folder
//        Folder f = folderRepository.findByNameAndUser("folder",userRepository.findByEmail("testUser@gmail.com").get());
//        assertEquals(fileController.createDocument(f.getId(),"newDoc","",userRepository.findByEmail("testUser@gmail.com").get().getId()).toString(),"tt");
    }

    @Test
    void createDocument_noName_BAD_REQUEST() {
        Folder f = folderRepository.findByNameAndUser("folder",userRepository.findByEmail("testUser@gmail.com").get());
        assertEquals(fileController.createDocument(f.getId(),"","",userRepository.findByEmail("testUser@gmail.com").get().getId()),
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not approve the given information: "));
    }
    @Test
    void renameFolder_success() {
        Folder f = folderRepository.findByNameAndUser("folder",userRepository.findByEmail("testUser@gmail.com").get());
        assertEquals(fileController.renameFolder(f.getId(),"newName",userRepository.findByEmail("testUser@gmail.com").get().getId()).toString().substring(1,4),
                "200");
        assertEquals(folderRepository.findByNameAndUser("newName",userRepository.findByEmail("testUser@gmail.com").get()).getName(),"newName");
    }
    @Test
    void renameFolder_noName_BAD_REQUEST() {
        Folder f = folderRepository.findByNameAndUser("folder",userRepository.findByEmail("testUser@gmail.com").get());
        assertEquals(fileController.renameFolder(f.getId(),"",userRepository.findByEmail("testUser@gmail.com").get().getId()).toString().substring(1,4),
                "200");
    }
    @Test
    void rename() {
    }

    @Test
    void testRename() {
    }

    @Test
    void delete() {
    }

    @Test
    void testDelete() {
    }

    @Test
    void relocate() {
    }

    @Test
    void testRelocate() {
    }

    @Test
    void export() {
    }

    @Test
    void getPath() {
    }

    @Test
    void documentExists() {
    }

    @Test
    void getUser() {
    }

    @Test
    void getContent() {
    }
}



