package docSharing.controller;

import com.google.api.services.gmail.Gmail;
import docSharing.entity.Document;
import docSharing.entity.Folder;
import docSharing.entity.Permission;
import docSharing.entity.User;
import docSharing.requests.Method;
import docSharing.response.UsersInDocRes;
import docSharing.service.DocumentService;
import docSharing.service.UserService;
import docSharing.utils.EmailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    private static Gmail service;
    @InjectMocks
    private FacadeUserController facadeUserController;
    @InjectMocks
    private EmailUtil emailUtil;
    @Spy
    private List<UsersInDocRes> usersInDoc = new ArrayList<>();

    private User goodUser;
    private Document document;
    private Folder folder;


    @BeforeEach
    @DisplayName("Make sure all the correct parameters are refreshed after each operation")
    void setUp() {
        goodUser = User.createUser("asaf396@gmai.com", "dvir1234", "dvir");
        goodUser.setId(1L);
        folder = new Folder();
        document = Document.createDocument(goodUser, "hello", folder, "over");
    }

    @Test
    @DisplayName("Trying to give a permission, without a permission parameter, will throw an exception")
    void givePermission_LackPermission_throwsNullArgumentException() {
        assertEquals(400, facadeUserController.givePermission(1L, 1L, null).getStatusCode(), "Trying to change permission to user with a null permission did not throw errorCode 400");
    }

    @Test
    @DisplayName("Given all the correct parameters, when trying to give permission to a user, successfully change his permission")
    void givePermision_successful() throws AccountNotFoundException {
        UserService us = mock(UserService.class);
        given(documentService.getAllUsersInDocument(1L, 1L, Method.GET)).willReturn(usersInDoc);
        assertEquals(200, facadeUserController.givePermission(1L, 1L, Permission.UNAUTHORIZED).getStatusCode(), "When granting permission to user, didn't get statusCode 200");
    }

    @Test
    @DisplayName("Trying to remove any permissions to a user using his ID, will lead to a successful remove from the database")
    void givePermission_removeFromPermissions_successful() {
        assertEquals(200, facadeUserController.givePermission(1L, 1L, Permission.UNAUTHORIZED).getStatusCode(), "change permission to unregistered user did not return responseCode 400");
    }

    @Test
    @DisplayName("Given a wrong document ID, when trying to remove a permission to a user, lead to an exception to be thrown")
    void givePermission_wrongDocumentId_BAD_REQUEST() throws AccountNotFoundException {
        doThrow(IllegalArgumentException.class).when(documentService).getAllUsersInDocument(1L, 1L, Method.GET);
        assertEquals(400, facadeUserController.givePermission(1L, 1L, Permission.UNAUTHORIZED).getStatusCode(), "change permission to non-existing document did not return responseCode 400");
    }

    @Test
    @DisplayName("Give permission to a list of users will lead to a successful operation if all the parameters are correct")
    void givePermissionToAll_EmptyList_successful() {
        List<String> emails = new ArrayList<>();
        assertEquals(200, facadeUserController.givePermissionToAll(emails, 1L).getStatusCode());
    }

    @Test
    @DisplayName("Given wrong emails to grant permission will lead to a bad request response")
    void givePermissionToAll_wrongEmailsOrUnregistered_BAD_REQUEST() {
        List<String> emails = new ArrayList<>();
        assertEquals(200, facadeUserController.givePermissionToAll(emails, 1L).getStatusCode());
    }

    @Test
    @DisplayName("When an unauthorized user tries to change permissions to users, a ")
    void givePermissionToAll_inviteUnauthorizedUser_OK() throws AccountNotFoundException, FileNotFoundException {
        List<String> emails = new ArrayList<>();
        emails.add("ziv@gmail.com");
        given(userService.findByEmail("ziv@gmail.com")).willReturn(goodUser);
        given(documentService.getUserPermissionInDocument(1L, 1L)).willReturn(Permission.UNAUTHORIZED);
        given(documentService.findById(1L)).willReturn(document);
        assertEquals(200, facadeUserController.givePermissionToAll(emails, 1L).getStatusCode());
    }

    @Test
    @DisplayName("When a registered user asks for his documents, given all correct in formation, successfully return all documents to him")
    void getDocumentsForUser_whenUserExists_successful() {
        assertEquals(200, facadeUserController.getDocuments(1L).getStatusCode());
    }


    @Test
    @DisplayName("If a user doesn't exist in the database, don't return his files and throw an exception / bad request response")
    void getDocumentsForUser_whenUserDoesntExist_BAD_REQUEST() throws AccountNotFoundException {
        given(userService.documentsOfUser(1L)).willThrow(AccountNotFoundException.class);
        assertEquals(400, facadeUserController.getDocuments(1L).getStatusCode());
    }

    @Test
    @DisplayName("Once trying to fetch a user information, given an existing user, successfully return his data")
    void getUser_whenUserExists_successful() {
        assertEquals(200, facadeUserController.getUser(1L).getStatusCode());
    }

    @Test
    @DisplayName("Once trying to fetch a user information, given a non-existing user, fail to return his data")
    void getUser_whenUserDoesntExist_BAD_REQUEST() throws AccountNotFoundException {
        given(userService.getUser(1L)).willThrow(AccountNotFoundException.class);
        assertEquals(400, facadeUserController.getUser(1L).getStatusCode());
    }

    @Test
    @DisplayName("When trying to fetch a user permission for a specific document, given all the correct information, return a good response")
    void getUserPermissionForDocument_whenUserExists_succesful() throws AccountNotFoundException {
        given(userService.findById(goodUser.getId())).willReturn(goodUser);
        assertEquals(200, facadeUserController.getUserPermissionForSpecificDocument(1L, 1L).getStatusCode());
    }

    @Test
    @DisplayName("When trying to get a permission of a user for a specific document, if he doesn't exist, return a bad request")
    void getUserPermissionForDocument_whenUserDoesntExist_fail() throws AccountNotFoundException {
        given(userService.findById(1L)).willThrow(AccountNotFoundException.class);
        assertEquals(400, facadeUserController.getUserPermissionForSpecificDocument(1L, 1L).getStatusCode());
    }

    @Test
    @DisplayName("When trying to get a permission of a user for a specific document, and the document doesn't exist, return a bad request")
    void getUserPermissionForDocument_whenDocumentDoesntExist_fail() throws FileNotFoundException, AccountNotFoundException {
        given(documentService.getUserPermissionInDocument(1L, 1L)).willThrow(FileNotFoundException.class);
        assertEquals(400, facadeUserController.getUserPermissionForSpecificDocument(1L, 1L).getStatusCode());
    }
}
