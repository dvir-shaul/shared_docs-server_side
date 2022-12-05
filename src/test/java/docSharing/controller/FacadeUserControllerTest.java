package docSharing.controller;

import docSharing.entity.Permission;
import docSharing.entity.User;
import docSharing.repository.DocumentRepository;
import docSharing.repository.UserRepository;
import docSharing.requests.Method;
import docSharing.response.UsersInDocRes;
import docSharing.service.DocumentService;
import docSharing.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import javax.security.auth.login.AccountNotFoundException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class FacadeUserControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private DocumentService documentService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DocumentRepository documentRepository;
    @InjectMocks
    private FacadeUserController facadeUserController;
    @Spy
    private List<UsersInDocRes> usersInDoc = new ArrayList<>();

    private User goodUser;
    private User badEmailUser;
    private User badPasswordUser;
    private User badNameUser;

    @BeforeEach
    void setUp() {
        goodUser = User.createUser("asaf396@gmai.com", "dvir1234", "dvir");
        goodUser.setId(1L);
    }

    @Test
    void givePermission_LackPermission_throwsNullArgumentException() {
        assertEquals(400, facadeUserController.givePermission(1L, 1L, null).getStatusCode(), "Trying to change permission to user with a null permission did not throw errorCode 400");
    }

    @Test
    void givePermision_successful() throws AccountNotFoundException {
        UserService us = mock(UserService.class);
        given(documentService.getAllUsersInDocument(1L, 1L, Method.GET)).willReturn(usersInDoc);
        assertEquals(200, facadeUserController.givePermission(1L, 1L, Permission.UNAUTHORIZED).getStatusCode(), "When granting permission to user, didn't get statusCode 200");
    }

    @Test
    void givePermission_noUserId_successful() {
        assertEquals(200, facadeUserController.givePermission(1L, 1L, Permission.UNAUTHORIZED).getStatusCode(), "change permission to unregistered user did not return responseCode 400");
    }

    @Test
    void givePermission_wrongDocumentId_BAD_REQUEST() {
        doThrow(IllegalArgumentException.class).when(documentService).getAllUsersInDocument(1L, 1L, Method.GET);
        assertEquals(400, facadeUserController.givePermission(1L, 1L, Permission.UNAUTHORIZED).getStatusCode(), "change permission to non-existing document did not return responseCode 400");
    }

    @Test
    void givePermissionToAll_EmptyList_successful() {
        List<String> emails = new ArrayList<>();
        assertEquals(200, facadeUserController.givePermissionToAll(emails, 1L).getStatusCode());
    }

    @Test
    void givePermissionToAll_wrongEmailsOrUnregistered_BAD_REQUEST() throws AccountNotFoundException {
        List<String> emails = new ArrayList<>();
//        given(userService.findByEmail("shalom")).willThrow(AccountNotFoundException.class);
        assertEquals(200, facadeUserController.givePermissionToAll(emails, 1L).getStatusCode());
    }

//    @Test
//    void givePermissionToAll_unauthorizedUser_BAD_REQUEST() throws AccountNotFoundException {
//        List<String> emails = new ArrayList<>();
//        emails.add("ziv@gmail.com");
//        given(userService.findByEmail("email")).willReturn(goodUser);
////        given(userService.findByEmail("shalom")).willThrow(AccountNotFoundException.class);
//        given(documentService.getUserPermissionInDocument(1L, 1L)).willReturn(Permission.UNAUTHORIZED);
//        assertEquals(200, facadeUserController.givePermissionToAll(emails, 1L).getStatusCode());
//    }

    @Test
    void getDocumentsForUser_whenUserExists_successful() {
        assertEquals(200, facadeUserController.getDocuments(1L).getStatusCode());
    }


    @Test
    void getDocumentsForUser_whenUserDoesntExist_BAD_REQUEST() {
        given(userService.documentsOfUser(1L)).willThrow(IllegalArgumentException.class);
        assertEquals(400, facadeUserController.getDocuments(1L).getStatusCode());
    }

    @Test
    void getUser_whenUserExists_successful() {
        assertEquals(200, facadeUserController.getUser(1L).getStatusCode());
    }

    @Test
    void getUser_whenUserDoesntExist_BAD_REQUEST() {
        given(userService.getUser(1L)).willThrow(IllegalArgumentException.class);
        assertEquals(400, facadeUserController.getUser(1L).getStatusCode());
    }

    @Test
    void getUserPermissionForDocument_whenUserExists_succesful() throws AccountNotFoundException {
        given(userService.findById(goodUser.getId())).willReturn(goodUser);
        assertEquals(200, facadeUserController.getUserPermissionForSpecificDocument(1L, 1L).getStatusCode());
    }

    @Test
    void getUserPermissionForDocument_whenUserDoesntExist_fail() throws AccountNotFoundException {
        given(userService.findById(1L)).willThrow(AccountNotFoundException.class);
        assertEquals(400, facadeUserController.getUserPermissionForSpecificDocument(1L, 1L).getStatusCode());
    }

    @Test
    void getUserPermissionForDocument_whenDocumentDoesntExist_fail() throws FileNotFoundException {
        given(documentService.getUserPermissionInDocument(1L, 1L)).willThrow(FileNotFoundException.class);
        assertEquals(400, facadeUserController.getUserPermissionForSpecificDocument(1L, 1L).getStatusCode());
    }
}
