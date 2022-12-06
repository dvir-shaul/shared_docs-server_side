package docSharing.service;


import docSharing.entity.*;
import docSharing.repository.*;
import docSharing.requests.Method;
import docSharing.utils.logAction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.security.auth.login.AccountNotFoundException;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.BDDMockito.given;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
    @InjectMocks
    private DocumentService documentService;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    FolderRepository folderRepository;
    @Mock
    UserDocumentRepository userDocumentRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    LogRepository logRepository;

    private Document document;
    private Folder folder;
    private User user;
    private UserDocument userDocument;
    private Log log;

    @BeforeEach
    public void createFolder() {
        user = User.createUser("test@test.com", "test1234", "test");
        folder = Folder.createFolder("test", null);
        document = Document.createDocument(user, "testDoc", folder, "test");
        document.setId(1l);
        userDocument=new UserDocument();
        userDocument.setDocument(document);
        userDocument.setUser(user);
        log=new Log();
        log.setDocument(document);
        log.setData("test");
        log.setUser(user);
        log.setLastEditDate(LocalDateTime.now());
        log.setOffset(1);
        log.setAction(logAction.INSERT);
        log.setId(1l);
    }

    @Test
    @DisplayName("Make sure a list of active user contains the user when calling getActiveUsers with valid parameters and ADD method")
    public void getActiveUsers_validParametersMethodADD_returnUsers() throws AccountNotFoundException {
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        assertTrue(documentService.getActiveUsers(user.getId(), document.getId(), Method.ADD).contains(user));
    }

    @Test
    @DisplayName("Make sure a list of active user does not contain the user when calling getActiveUsers with valid parameters and REMOVE method")
    public void getActiveUsers_validParametersMethodREMOVE_returnUsers() throws AccountNotFoundException {
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        assertFalse(documentService.getActiveUsers(user.getId(), document.getId(), Method.REMOVE).contains(user));
    }
    @Test
    @DisplayName("Make sure a list of active user is returned when calling getActiveUsers with valid parameters and GET method")
    public void getActiveUsers_validParametersMethodGET_returnUsers() throws AccountNotFoundException {
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        assertNotNull(documentService.getActiveUsers(user.getId(), document.getId(), Method.GET));
    }
    @Test
    @DisplayName("Make sure an exception is thrown when calling getActiveUsers with invalid parameters")
    public void getActiveUsers_invalidParameters_throwsAccountNotFound() {
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class,()-> documentService.getActiveUsers(user.getId(), document.getId(), Method.GET));
    }
    @Test
    @DisplayName("Make sure path of document is returned when calling getPath with valid parameters")
    public void getPath_validParameters_returnsPath() throws FileNotFoundException {
        given(documentRepository.findById(document.getId())).willReturn(Optional.of(document));
        assertNotNull(documentService.getPath(document.getId()));
    }
    @Test
    @DisplayName("Make sure an exception is thrown when calling getPath with invalid parameters")
    public void getPath_documentNotExist_throwsFileNotFound() throws FileNotFoundException {
        given(documentRepository.findById(document.getId())).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class,()->documentService.getPath(document.getId()));
    }
    @Test
    @DisplayName("Make sure permission is returned when calling getUserPermissionInDocument with valid parameters")
    public void getUserPermissionInDocument_validParameters_returnPermission() throws FileNotFoundException, AccountNotFoundException {
        given(documentRepository.findById(document.getId())).willReturn(Optional.of(document));
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userDocumentRepository.find(document, user)).willReturn(Optional.of(userDocument));
        assertNotNull(documentService.getUserPermissionInDocument(user.getId(), document.getId()));
    }
    @Test
    @DisplayName("Make sure permission UNAUTHORIZED is returned when calling getUserPermissionInDocument when user does not have permission in a document")
    public void getUserPermissionInDocument_userInDocumentNotExists_returnUNAUTHORIZED() throws FileNotFoundException, AccountNotFoundException {
        given(documentRepository.findById(document.getId())).willReturn(Optional.of(document));
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userDocumentRepository.find(document, user)).willReturn(Optional.empty());
        assertEquals(Permission.UNAUTHORIZED,documentService.getUserPermissionInDocument(user.getId(), document.getId()));
    }
    @Test
    @DisplayName("Make sure an exception is thrown when calling getUserPermissionInDocument when user does not exist")
    public void getUserPermissionInDocument_userNotExists_throwAccountNotFound() throws FileNotFoundException {
        given(documentRepository.findById(document.getId())).willReturn(Optional.of(document));
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, ()->documentService.getUserPermissionInDocument(user.getId(), document.getId()));
    }
    @Test
    @DisplayName("Make sure an exception is thrown when calling getUserPermissionInDocument when document does not exist")
    public void getUserPermissionInDocument_documentNotExists_throwFileNotFound() throws FileNotFoundException {
        given(documentRepository.findById(document.getId())).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, ()->documentService.getUserPermissionInDocument(user.getId(), document.getId()));
    }
    @Test
    @DisplayName("Make sure user-document is being saved when calling saveUserInDocument with valid parameters")
    public void saveUserInDocument_validParameters_returnUserInDocument(){
        assertNotNull(documentService.saveUserInDocument(userDocument));
    }
    @Test
    @DisplayName("Make sure an exception is thrown when calling saveUserInDocument with null")
    public void saveUserInDocument_null_throwNullPointerException(){
       assertThrows(NullPointerException.class,()->documentService.saveUserInDocument(null));
    }
    @Test
    @DisplayName("Make sure content is updated when calling updateContent with valid log")
    public void updateContent_validLog_updateContent(){
        given(documentRepository.findById(log.getDocument().getId())).willReturn(Optional.of(document));
    }
}