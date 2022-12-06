package docSharing.service;

import docSharing.entity.*;
import docSharing.repository.DocumentRepository;
import docSharing.repository.UserDocumentRepository;
import docSharing.repository.UserRepository;
import docSharing.response.FileRes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import javax.security.auth.login.AccountNotFoundException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    DocumentRepository documentRepository;
    @Mock
    UserDocumentRepository userDocumentRepository;
    @InjectMocks
    private UserService userService;

    private User user;
    private Document document;
    private Folder folder;
    private UserDocument userDocument;
    private UserDocumentPk userDocumentPk;


    @BeforeEach
    void setUp() {
        user = User.createUser("asaf396@gmai.com", "dvir1234", "dvir");
        user.setId(1L);
        folder = new Folder();
        folder.setId(1L);
        userDocumentPk = new UserDocumentPk();
        userDocumentPk.setUserId(1L);
        document = Document.createDocument(user, "hey", folder, null);
        document.setId(1L);
        userDocument = new UserDocument();
    }

    @Test
    @DisplayName("Make sure user is returned when calling findById with existing user id")
    void findById_givenId_getUser() throws AccountNotFoundException {
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        assertEquals(user, userService.findById(1L));
    }

    @Test
    @DisplayName("Make sure an exception is thrown when calling findById with non-existing user id")
    void findById_givenIdThatDoesntExist_throwsAccountNotFoundException() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> userService.findById(1L), "Getting an account that doesn't exist in the system is not a legal option");
    }

    @Test
    @DisplayName("Make user is returned when calling findByEmail with existing user email")
    void findByEmail_givenEmail_getUser() throws AccountNotFoundException {
        given(userRepository.findByEmail("email")).willReturn(Optional.of(user));
        assertEquals(user, userService.findByEmail("email"));
    }

    @Test
    @DisplayName("Make sure an exception is thrown when calling findByEmail with non-existing user email")
    void findByEmail_givenWrongEmailThatDoesntExist_throwsAccountNotFoundException() {
        given(userRepository.findByEmail("ziv")).willReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> userService.findByEmail("ziv"), "Getting an account that doesn't exist in the system is not a legal option");
    }

    @Test
    @DisplayName("Make sure an exception is thrown when calling updatePermission with non-existing user id")
    void updatePermission_noneExistingUser_throwsAccountNotFoundException() {
        given(documentRepository.findById(1L)).willReturn(Optional.of(document));
        given(userRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> userService.updatePermission(1L, 1L, Permission.UNAUTHORIZED), "Getting an account that doesn't exist in the system is not a legal option");
    }

    @Test
    @DisplayName("Make sure an exception is thrown when calling updatePermission with non-existing document id")
    void updatePermission_noneExsistingDocuement_throwsIllegalArgumentException() {
        given(documentRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> userService.updatePermission(1L, 1L, Permission.UNAUTHORIZED));
    }

    @Test
    @DisplayName("Make 1 is returned when calling updatePermission with valid parameters")
    void updatePermission_createNewPermissionToUser_storeInDB() throws AccountNotFoundException, FileNotFoundException {
        given(documentRepository.findById(1L)).willReturn(Optional.of(document));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userDocumentRepository.find(document, user)).willReturn(Optional.empty());
        assertEquals(1, userService.updatePermission(1L, 1L, Permission.VIEWER));
    }

    @Test
    @DisplayName("Make sure permission is being changed when calling updatePermission with valid parameters")
    void updatePermission_changePermissionToUser_storeInDB() throws AccountNotFoundException, FileNotFoundException {
        given(documentRepository.findById(1L)).willReturn(Optional.of(document));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userDocumentRepository.find(document, user)).willReturn(Optional.of(userDocument));
        given(userDocumentRepository.updatePermission(Permission.VIEWER, document, user)).willReturn(1);
        assertEquals(1, userService.updatePermission(1L, 1L, Permission.VIEWER));
    }

    @Test
    @DisplayName("Make sure permission is being removed when calling updatePermission with valid parameters")
    void updatePermission_removeUserFromPermissionsList_success() throws AccountNotFoundException, FileNotFoundException {
        given(documentRepository.findById(1L)).willReturn(Optional.of(document));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userDocumentRepository.deleteUserFromDocument(user, document)).willReturn(1);
        assertEquals(1, userService.updatePermission(1L, 1L, Permission.UNAUTHORIZED));
    }

    @Test
    @DisplayName("Make exception is being thrown when calling documentsOfUser with invalid parameters")
    void documentsOfUser_givenWrongId_throwAccountNotFound() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> userService.documentsOfUser(1L));
    }

    @Test
    @DisplayName("Make sure a list of documents is returned when calling documentsOfUser with valid parameters")
    void documentsOfUser_givenCorrectUserId_getAllFiles() throws AccountNotFoundException {
        List<FileRes> files = new ArrayList<>();
        List<UserDocument> userDocuments = new ArrayList<>();
        UserDocument ud = new UserDocument(userDocumentPk, document, user, Permission.VIEWER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userDocumentRepository.findByUser(user)).willReturn(userDocuments);
        assertEquals(files, userService.documentsOfUser(1L));
    }

    @Test
    @DisplayName("Make sure a user is returned when calling getUser with valid parameters")
    void getUser_givenCorrectId_success() throws AccountNotFoundException {
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        assertEquals(user.getEmail(), userService.getUser(1L).getEmail());
        assertEquals(user.getName(), userService.getUser(1L).getName());
        assertEquals(user.getId(), userService.getUser(1L).getId());
    }

    @Test
    @DisplayName("Make an exception is being thrown when calling getUser with invalid parameters")
    void getUser_givenNonExistingUserId_fail() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> userService.getUser(1L));
    }
}
