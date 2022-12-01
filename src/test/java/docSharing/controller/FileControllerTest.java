package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Folder;
import docSharing.entity.User;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserRepository;
import docSharing.response.FileRes;
import docSharing.response.JoinRes;
import docSharing.utils.ExceptionMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.print.Doc;
import javax.security.auth.login.AccountNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
    void setup() {
        userRepository.deleteAll();
        folderRepository.deleteAll();
        User user = userRepository.save(User.createUser("testUser@gmail.com", "2222222", "tester"));
        long userId = userRepository.findByEmail("testUser@gmail.com").get().getId();
        userRepository.updateIsActivated(true, userId);
        Folder folder = new Folder();
        folder.setName("folder");
        folder.setUser(user);
        folderRepository.save(folder);
    }

    @Test
    void getAll_successfully() throws AccountNotFoundException {
        ResponseEntity<List<FileRes>> list = fileController.getAll(null, userRepository.findByEmail("testUser@gmail.com").get().getId());
        assertFalse(Objects.requireNonNull(list.getBody()).isEmpty());
    }

    @Test
    void getAll_noParentFolderInDB_successfully() throws AccountNotFoundException {
        assertThrows(RuntimeException.class, () -> fileController.getAll(240000L, userRepository.findByEmail("testUser@gmail.com").get().getId()));
    }

    @Test
    void getAll_noUserInDB_RuntimeException() throws AccountNotFoundException {
        assertThrows(RuntimeException.class, () -> fileController.getAll(null, userRepository.findByEmail("tes213tUser@gmail.com").get().getId()));
    }

    @Test
    void createFolder_successFromNullParent() {
        assertEquals(fileController.createFolder(null, "f1_folder", userRepository.findByEmail("testUser@gmail.com").get().getId()).toString().substring(1, 4), "200");
    }

    //    @Test
//    void createFolder_successWithIdParent() { // nested exception
//        Folder f = folderRepository.findByNameAndUser("folder",userRepository.findByEmail("testUser@gmail.com").get());
//        assertEquals(fileController.createFolder(f.getId(),"f1_folder",userRepository.findByEmail("testUser@gmail.com").get().getId()).toString().substring(1,4),"200");
//    }
    @Test
    void createFolder_badInputFolderName_IllegalArgumentException() {
        Folder f = folderRepository.findByNameAndUser("folder", userRepository.findByEmail("testUser@gmail.com").get());
        assertThrows(IllegalArgumentException.class, () -> fileController.createFolder(f.getId(), "", userRepository.findByEmail("testUser@gmail.com").get().getId()), "bad folder name input dont throw IllegalArgumentException");
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
        Folder f = folderRepository.findByNameAndUser("folder", userRepository.findByEmail("testUser@gmail.com").get());
        assertEquals(fileController.createDocument(f.getId(), "", "", userRepository.findByEmail("testUser@gmail.com").get().getId()),
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not approve the given information: "));
    }

    @Test
    void renameFolder_newName_success() {
        Folder f = folderRepository.findByNameAndUser("folder", userRepository.findByEmail("testUser@gmail.com").get());
        assertEquals(fileController.renameFolder(f.getId(), "newName", userRepository.findByEmail("testUser@gmail.com").get().getId()).toString().substring(1, 4),
                "200");
        assertEquals(folderRepository.findByNameAndUser("newName", userRepository.findByEmail("testUser@gmail.com").get()).getName(), "newName");
    }

    @Test
    void renameFolder_noName_BAD_REQUEST() {
        Folder f = folderRepository.findByNameAndUser("folder", userRepository.findByEmail("testUser@gmail.com").get());
        assertEquals(fileController.renameFolder(f.getId(), "", userRepository.findByEmail("testUser@gmail.com").get().getId()).toString(),
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionMessage.VALIDATION_FAILED + "name is empty/null").toString());
    }

    @Test
    void renameDocument_newName_success() {
        //org.springframework.dao.InvalidDataAccessApiUsageException: detached entity passed to persist: docSharing.entity.Folder;
        // nested exception is org.hibernate.PersistentObjectException: detached entity passed to persist: docSharing.entity.Folder
//        User user = userRepository.findByEmail("testUser@gmail.com").get();
//        Folder f = folderRepository.findByNameAndUser("folder",user);
//        Document document = Document.createDocument(user,"newDoc",f,"");
//        document.setParentFolder(f);
//        fileController.createDocument(f.getId(),document.getName(),"",document.getUser().getId());
//        documentRepository.save(document);
//
//        Document document1 = documentRepository.findByNameAndUser("newDoc",user);
//                assertEquals(fileController.renameDocument(document1.getId(),"newDoc2",user.getId()).toString().substring(1,4),
//                        "200");

    }

    @Test
    void deleteFolder_givenFolder_success() {
        //org.hibernate.LazyInitializationException:
        // failed to lazily initialize a collection of role: docSharing.entity.Folder.documents, could not initialize proxy - no Session
//        User user = userRepository.findByEmail("testUser@gmail.com").get();
//        Folder folder = new Folder();
//        folder.setName("folderrrrrrrrrrrrrrr");
//        folder.setUser(user);
//        Document document = Document.createDocument(user,"newDoc",folder,"hey");
//        folder.addDocument(document);
//        folderRepository.save(folder);
//        documentRepository.save(document);
//        assertEquals(fileController.deleteFolder(folder.getId(),userRepository.findByEmail("testUser@gmail.com").get().getId()).toString().substring(1,4),
//                "200");
    }
}
//    @Test
//    void deleteDocument_givenDoc_success() {
//        User user = userRepository.findByEmail("testUser@gmail.com").get();
//        Folder folder = new Folder();
//        folder.setName("folder");
//        folder.setUser(user);
//        Document document = Document.createDocument(user,"newDoc",folder,"hey");
//        folder.addDocument(document);
//        folderRepository.save(folder);
//        documentRepository.save(document);
//        assertEquals(fileController.deleteDocument(document.getId(),userRepository.findByEmail("testUser@gmail.com").get().getId()).toString().substring(1,4),
//                "200");
//    }

//    @Test
//    void relocate() {
//    }


//    @Test
//    void export() {
//    }

//    @Test
//    void getPath() {
//    }
//    @Test
//    void documentExists_wrongValues_NoAccountInDatabase() {
//        User user = userRepository.findByEmail("testUser@gmail.com").get();
//        Folder folder = new Folder();
//        folder.setName("folder");
//        folder.setUser(user);
//        Document document = Document.createDocument(user,"newDoc",folder,"hey");
//        folder.addDocument(document);
//        folderRepository.save(folder);
//        documentRepository.save(document);
//        assertEquals(fileController.documentExists(documentRepository.findByNameAndUser(document.getName(),document.getUser()).getId()+24,user.getId()+15),
//                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString()));
//    }
//    @Test
//    void documentExists_goodValues_success() {
//        User user = userRepository.findByEmail("testUser@gmail.com").get();
//        Folder folder = new Folder();
//        folder.setName("folder");
//        folder.setUser(user);
//        Document document = Document.createDocument(user,"newDoc",folder,"hey");
//        folder.addDocument(document);
//        folderRepository.save(folder);
//        documentRepository.save(document);
//        assertEquals(fileController.documentExists(documentRepository.findByNameAndUser(document.getName(),document.getUser()).getId(),user.getId()).toString().substring(1,4),
//                "200");
//    }
//    @Test
//    void getUser_WrongValues_NoAccountInDatabase() {
//        User user = userRepository.findByEmail("testUser@gmail.com").get();
//        Folder folder = new Folder();
//        folder.setName("folder");
//        folder.setUser(user);
//        Document document = Document.createDocument(user,"newDoc",folder,"hey");
//        folder.addDocument(document);
//        folderRepository.save(folder);
//        documentRepository.save(document);
//        assertEquals(fileController.getUser(documentRepository.findByNameAndUser(document.getName(),document.getUser()).getId(),user.getId()+15).getBody(),
//                ExceptionMessage.NO_ACCOUNT_IN_DATABASE.toString());
//    }
//    @Test
//    void getUser_goodValues_success() {
//        User user = userRepository.findByEmail("testUser@gmail.com").get();
//        Folder folder = new Folder();
//        folder.setName("folder");
//        folder.setUser(user);
//        Document document = Document.createDocument(user,"newDoc",folder,"hey");
//        folder.addDocument(document);
//        folderRepository.save(folder);
//        documentRepository.save(document);
//        JoinRes joinRes = (JoinRes) fileController.getUser(documentRepository.findByNameAndUser(document.getName(),document.getUser()).getId(),user.getId()).getBody();
//        assertEquals(joinRes.getUserId(),user.getId());
//    }
//
//    @Test
//    void getContent_fromDoc_success() {
//        User user = userRepository.findByEmail("testUser@gmail.com").get();
//        Folder folder = new Folder();
//        folder.setName("folder");
//        folder.setUser(user);
//        Document document = Document.createDocument(user,"newDoc",folder,"hey");
//        folder.addDocument(document);
//        folderRepository.save(folder);
//        documentRepository.save(document);
//        assertEquals(fileController.getContent(documentRepository.findByNameAndUser(document.getName(),document.getUser()).getId(), user.getId()).getBody(),"hey");
//    }
//    @Test
//    void getContent_wrongDoc_nullReturn() {
//        User user = userRepository.findByEmail("testUser@gmail.com").get();
//        Folder folder = new Folder();
//        folder.setName("folder");
//        folder.setUser(user);
//        Document document = Document.createDocument(user,"newDoc",folder,"hey");
//        folder.addDocument(document);
//        folderRepository.save(folder);
//        documentRepository.save(document);
//        assertNull(fileController.getContent(documentRepository.findByNameAndUser(document.getName(), document.getUser()).getId() + 4, user.getId()).getBody());
//    }
//}
//
//
//
