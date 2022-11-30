//package docSharing.controller;
//
//import docSharing.entity.Folder;
//import docSharing.entity.User;
//import docSharing.repository.DocumentRepository;
//import docSharing.repository.FolderRepository;
//import docSharing.repository.UserRepository;
//import docSharing.requests.CreateDocumentReq;
//import docSharing.requests.CreateFolderReq;
//import docSharing.response.FileRes;
//import docSharing.service.DocumentService;
//import docSharing.service.FolderService;
//import docSharing.service.UserService;
//import docSharing.utils.ConfirmationToken;
//import docSharing.utils.ExceptionMessage;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.api.function.Executable;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import javax.security.auth.login.AccountNotFoundException;
//
//import java.util.List;
//import java.util.Objects;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@ExtendWith(SpringExtension.class)@SpringBootTest
//class FileControllerTest {
//
//    @Autowired
//    FileController fileController;
//    @Autowired
//    FolderRepository folderRepository;
//    @Autowired
//    AuthController authController;
//    @Autowired
//    UserRepository userRepository;
//    @Autowired
//    DocumentRepository documentRepository;
//    @Autowired
//    AbstractController ac;
//    @Autowired
//    FolderService folderService;
//    @Autowired
//    DocumentService documentService;
//    @Autowired
//    UserService userService;
//
//    private User goodUser;
//    @Test
//    void OrgSetup() throws AccountNotFoundException {
//        userRepository.deleteAll();
//        folderRepository.deleteAll();
//        documentRepository.deleteAll();
//        CreateFolderReq a = new CreateFolderReq("first_folder",null);
//        goodUser=User.createUser("asaf396@gmai.com","dvir1234","dvir");
//        register(goodUser);
//        goodUser = userRepository.findByEmail(goodUser.getEmail()).get();
//        fileController.createFolder(a,goodUser.getId());
//    }
//    public void register(User user) throws AccountNotFoundException {
//        authController.register(user);
//        Long id = userRepository.findByEmail(user.getEmail()).get().getId();
//        String token = ConfirmationToken.createJWT(Long.toString(id), "docs-app", "activation email", 5*1000*60);
//        assertEquals(authController.activate(token),ResponseEntity.status(200).body("account activated successfully!"));
//    }
//
//    @Test
//    void getAll() throws AccountNotFoundException {
////        System.out.println("+++++++++++++++++++++");
////        ResponseEntity<List<FileRes>> list = fileController.getAll(null,77L);
////        assertFalse(Objects.requireNonNull(list.getBody()).isEmpty());
//    }
//    @Test
//    void createFolder_successFromNullParent() {
//        CreateFolderReq folderReq1 = new CreateFolderReq("f1_folder",null);
//        assertEquals(fileController.createFolder(goodUser.getId(),folderReq1.getName()).toString().substring(1,4),"200");
//    }
//    @Test
//    void createFolder_successWithIdParent() {
//        CreateFolderReq folderReq1 = new CreateFolderReq("sec1_folder",null);
//        Long id = Long.valueOf(fileController.createFolder(folderReq1, goodUser.getId()).toString().split(",")[1]);
//        CreateFolderReq folderReq2 = new CreateFolderReq("sec12_folder",id);
//        assertEquals(fileController.createFolder(folderReq2, goodUser.getId()).toString().substring(1,4),"200");
//    }
//    @Test
//    void createFolder_badInputFolderName_IllegalArgumentException() {
//        CreateFolderReq a = new CreateFolderReq("",null);
//        assertThrows(IllegalArgumentException.class,()->fileController.createFolder(a, goodUser.getId()),"bad folder name input dont throw IllegalArgumentException");
//    }
//    @Test
//    void createDocument_success() {
////        CreateFolderReq a = new CreateFolderReq("sec12_folder",null);
////        Long id = Long.valueOf(fileController.create(a, goodUser.getId()).toString().split(",")[1]);
////        CreateDocumentReq docReq = new CreateDocumentReq("firstDoc",id,"");
////        assertEquals(fileController.create(docReq, goodUser.getId()).toString().substring(1,4),"200");
//
//    }
//
//    @Test
//    void testCreate() {
//    }
//
//    @Test
//    void rename() {
//    }
//
//    @Test
//    void testRename() {
//    }
//
//    @Test
//    void delete() {
//    }
//
//    @Test
//    void testDelete() {
//    }
//
//    @Test
//    void relocate() {
//    }
//
//    @Test
//    void testRelocate() {
//    }
//
//    @Test
//    void export() {
//    }
//
//    @Test
//    void getPath() {
//    }
//
//    @Test
//    void documentExists() {
//    }
//
//    @Test
//    void getUser() {
//    }
//
//    @Test
//    void getContent() {
//    }
//}