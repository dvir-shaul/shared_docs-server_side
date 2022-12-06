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
import java.util.ArrayList;
import java.util.List;
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
        user.setId(1l);
        folder = Folder.createFolder("test", null);
        folder.setId(2l);
        document = Document.createDocument(user, "testDoc", folder, "test");
        document.setId(3l);
        userDocument = new UserDocument();
        userDocument.setDocument(document);
        userDocument.setUser(user);
        userDocument.setPermission(Permission.ADMIN);
        log = new Log();
        log.setDocument(document);
        log.setData("test");
        log.setUser(user);
        log.setLastEditDate(LocalDateTime.now());
        log.setOffset(0);
        log.setAction(logAction.INSERT);
        log.setId(4l);
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
        assertThrows(AccountNotFoundException.class, () -> documentService.getActiveUsers(user.getId(), document.getId(), Method.ADD));
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
        assertThrows(FileNotFoundException.class, () -> documentService.getPath(document.getId()));
    }

    @Test
    @DisplayName("Make sure permission is returned when calling getUserPermissionInDocument with valid parameters")
    public void getUserPermissionInDocument_validParameters_returnPermission() throws FileNotFoundException, AccountNotFoundException {
        given(documentRepository.findById(document.getId())).willReturn(Optional.of(document));
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userDocumentRepository.find(document, user)).willReturn(Optional.of(userDocument));
        assertEquals(userDocument.getPermission(), documentService.getUserPermissionInDocument(user.getId(), document.getId()));
    }

    @Test
    @DisplayName("Make sure permission UNAUTHORIZED is returned when calling getUserPermissionInDocument when user does not have permission in a document")
    public void getUserPermissionInDocument_userInDocumentNotExists_returnUNAUTHORIZED() throws FileNotFoundException, AccountNotFoundException {
        given(documentRepository.findById(document.getId())).willReturn(Optional.of(document));
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userDocumentRepository.find(document, user)).willReturn(Optional.empty());
        assertEquals(Permission.UNAUTHORIZED, documentService.getUserPermissionInDocument(user.getId(), document.getId()));
    }

    @Test
    @DisplayName("Make sure an exception is thrown when calling getUserPermissionInDocument when user does not exist")
    public void getUserPermissionInDocument_userNotExists_throwAccountNotFound() throws FileNotFoundException {
        given(documentRepository.findById(document.getId())).willReturn(Optional.of(document));
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> documentService.getUserPermissionInDocument(user.getId(), document.getId()));
    }

    @Test
    @DisplayName("Make sure an exception is thrown when calling getUserPermissionInDocument when document does not exist")
    public void getUserPermissionInDocument_documentNotExists_throwFileNotFound() throws FileNotFoundException {
        given(documentRepository.findById(document.getId())).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> documentService.getUserPermissionInDocument(user.getId(), document.getId()));
    }

    @Test
    @DisplayName("Make sure user-document is being saved when calling saveUserInDocument with valid parameters")
    public void saveUserInDocument_validParameters_returnUserInDocument() {
        given(userDocumentRepository.find(userDocument.getDocument(), userDocument.getUser())).willReturn(Optional.empty());
        given(userDocumentRepository.save(userDocument)).willReturn(userDocument);
        assertEquals(userDocument, documentService.saveUserInDocument(userDocument));
    }

    @Test
    @DisplayName("Make sure an exception is thrown when calling saveUserInDocument with null")
    public void saveUserInDocument_null_returnUserInDocument() {
        assertThrows(NullPointerException.class, () -> documentService.saveUserInDocument(null));
    }

    @Test
    @DisplayName("Make sure content is updated when calling updateContent with valid log")
    public void updateContent_validLog_updateContent() throws FileNotFoundException {
        given(documentRepository.findById(log.getDocument().getId())).willReturn(Optional.of(document));
        assertEquals("testtest", documentService.updateContent(log));
    }

    @Test
    public void updateContent_invalidLog_throwFileNotFoundException() {
        given(documentRepository.findById(log.getDocument().getId())).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> documentService.updateContent(log));
    }

    @Test
    public void updateContent_null_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> documentService.updateContent(null));
    }

    @Test
    public void findById_validId_returnsDocument() throws FileNotFoundException {
        given(documentRepository.findById(document.getId())).willReturn(Optional.of(document));
        assertEquals(document, documentService.findById(document.getId()));
    }

    @Test
    public void findById_invalidId_throwsFileNotFoundException() throws FileNotFoundException {
        given(documentRepository.findById(document.getId())).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> documentService.findById(document.getId()));
    }

    @Test
    public void get_validParameters_returnsListOfDocuments() throws AccountNotFoundException, FileNotFoundException {
        List<Document> documentList = new ArrayList<>();
        given(folderRepository.findById(folder.getId())).willReturn(Optional.of(folder));
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(documentRepository.findAllByUserIdAndParentFolderId(folder, user)).willReturn(documentList);
        assertEquals(documentList, documentService.get(folder.getId(), user.getId()));
    }

    @Test
    public void get_folderNotExist_throwsFileNotFoundException() {
        given(folderRepository.findById(folder.getId())).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> documentService.get(folder.getId(), user.getId()));
    }

    @Test
    public void get_userNotExist_throwsAccountNotFoundException() {
        given(folderRepository.findById(folder.getId())).willReturn(Optional.of(folder));
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> documentService.get(folder.getId(), user.getId()));
    }

//    @Test
//    public void create_validParameters_returnsId() throws FileNotFoundException {
//        Document spy = spy(Document.createDocument(document.getUser(), document.getName(), document.getParentFolder(), document.getContent()));
//        given(documentRepository.save(document)).willReturn(document);
//        given(userDocumentRepository.save(userDocument)).willReturn(userDocument);
//        assertEquals(document.getId(), documentService.create(folder, user, "test", "test"));
//    }

    @Test
    public void create_parentFolderNotExists_throwsFileNotFound() {
        given(folderRepository.findById(folder.getId())).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> documentService.create(folder, user, "test", "test"));
    }

    @Test
    public void create_parentFolderNull_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> documentService.create(null, user, "test", "test"));
    }

    @Test
    public void rename_validParameters_returns1() throws FileNotFoundException {
        given(documentRepository.findById(document.getId())).willReturn(Optional.of(document));
        given(documentRepository.updateName("newName", document.getId())).willReturn(1);
        assertEquals(1, documentService.rename(document.getId(), "newName"));
    }
    @Test
    public void rename_documentNotExist_throwsFileNotFoundException() {
        given(documentRepository.findById(document.getId())).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, ()->documentService.rename(document.getId(),"newName"));
    }
    @Test
    public void getContent_validId_returnContent() {
        given(documentRepository.findById(document.getId())).willReturn(Optional.of(document));
        assertDoesNotThrow(()->documentService.getContent(document.getId()));
    }
    @Test
    public void getContent_invalidId_throwFileNotFound() {
        given(documentRepository.findById(document.getId())).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class,()->documentService.getContent(document.getId()));
    }
    @Test
    public void concatenateStrings_validArguments_success() {
        assertDoesNotThrow(()->DocumentService.concatenateStrings("hello", log));
    }
    @Test
    public void concatenateStrings_nullTextArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, ()->DocumentService.concatenateStrings(null, log));
    }
    @Test
    public void concatenateStrings_nullLogArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, ()->DocumentService.concatenateStrings("hello", null));
    }
    @Test
    public void truncateString_validArguments_success() {
        assertDoesNotThrow(()->DocumentService.truncateString("hello", log));
    }
    @Test
    public void truncateString_nullTextArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, ()->DocumentService.truncateString(null, log));
    }
    @Test
    public void truncateString_nullLogArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, ()->DocumentService.truncateString("hello", null));
    }
    @Test
    public void doesExist_documentExist_returnsTrue() {
        given(documentRepository.findById(document.getId())).willReturn(Optional.of(document));
        assertTrue(documentService.doesExist(document.getId()));
    }
    @Test
    public void doesExist_folderDoesNotExist_returnsFalse() {
        given(documentRepository.findById(document.getId())).willReturn(Optional.empty());
        assertFalse(documentService.doesExist(document.getId()));
    }
}