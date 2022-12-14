package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Folder;
import docSharing.entity.User;
import docSharing.response.FileRes;
import docSharing.service.DocumentService;
import docSharing.service.FolderService;
import docSharing.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.BDDMockito.given;

import org.mockito.junit.jupiter.MockitoExtension;

import javax.security.auth.login.AccountNotFoundException;

import java.io.FileNotFoundException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FacadeFileControllerTest {

    @InjectMocks
    private FacadeFileController facadeFileController;

    @Mock
    private FolderService folderService;
    @Mock
    private DocumentService documentService;
    @Mock
    private UserService userService;
    private Document goodDocument;
    private Folder goodFolder;
    private User user;

    @BeforeEach
    void setup() {
        user = User.createUser("test@test.com", "test1234", "test");
        user.setId(1l);
        goodFolder = Folder.createFolder("testFolder", null, user);
        goodFolder.setId(2l);
        goodDocument = Document.createDocument(user, "test", goodFolder, null);
        goodDocument.setId(3l);
    }

    @Test
    @DisplayName("Given all the correct inputs, successfully create a new document")
    void createDocument_goodDocument_Successfully() throws AccountNotFoundException, FileNotFoundException {
        given(userService.findById(user.getId())).willReturn(user);
        given(documentService.create(null, user, "test", null)).willReturn(goodDocument.getId());
        assertEquals(201, facadeFileController.create(null, "test", null, user.getId(), Document.class).getStatusCode(), "create document with good parameters did not return 201");
    }

    @Test
    @DisplayName("Given a user ID who is unregistered to the system, fail when trying to create a new document")
    void createDocument_userIsNotExists_BadRequest() throws AccountNotFoundException {
        given(userService.findById(user.getId())).willThrow(new AccountNotFoundException("user is not exists"));
        assertEquals(400, facadeFileController.create(null, "test", null, user.getId(), Document.class).getStatusCode(), "create document with good parameters did not return 201");
    }

    @Test
    @DisplayName("Given a parent folder that does not exist in the database, fail when trying to create a new document")
    void createDocument_parentFolderNotExists_BadRequest() throws AccountNotFoundException, FileNotFoundException {
        given(folderService.findById(goodFolder.getId())).willThrow(new FileNotFoundException("folder is not exists"));
        assertEquals(400, facadeFileController.create(goodFolder.getId(), "test", null, user.getId(), Document.class).getStatusCode(), "create document with good parameters did not return 201");
    }

    @Test
    @DisplayName("Given all the correct inputs, successfully create a new folder")
    void createFolder_goodFolder_Successfully() throws AccountNotFoundException, FileNotFoundException {
        given(userService.findById(user.getId())).willReturn(user);
        given(folderService.create(null, user, "test", null)).willReturn(goodFolder.getId());
        assertEquals(201, facadeFileController.create(null, "test", null, user.getId(), Folder.class).getStatusCode(), "create document with good parameters did not return 201");
    }

    @Test
    @DisplayName("Given a user ID who is unregistered to the system, fail when trying to create a new folder")
    void createFolder_userIsNotExists_BadRequest() throws AccountNotFoundException {
        given(userService.findById(user.getId())).willThrow(new AccountNotFoundException("user is not exists"));
        assertEquals(400, facadeFileController.create(null, "test", null, user.getId(), Folder.class).getStatusCode(), "create document with good parameters did not return 201");
    }

    @Test
    @DisplayName("Given a parent folder that does not exist in the database, fail when trying to create a new folder")
    void createFolder_parentFolderNotExists_BadRequest() throws AccountNotFoundException, FileNotFoundException {
        given(folderService.findById(goodFolder.getId())).willThrow(new FileNotFoundException("folder is not exists"));
        assertEquals(400, facadeFileController.create(goodFolder.getId(), "test", null, user.getId(), Folder.class).getStatusCode(), "create document with good parameters did not return 201");
    }

    @Test
    @DisplayName("Given a correct document ID, successfully return a path for a document")
    void getPath_goodDocument_OK() throws FileNotFoundException {
        List<FileRes> path = new ArrayList<>();
        given(documentService.getPath(goodDocument.getId())).willReturn(path);
        assertEquals(200, facadeFileController.getPath(goodDocument.getId(), Document.class).getStatusCode(), "get path of existing document did not return 200");
    }

    @Test
    @DisplayName("Given an incorrect document ID, expect to throw a FileNotFound exception upon asking for a document's path")
    void getPath_badDocument_BadRequest() throws FileNotFoundException {
        given(documentService.getPath(goodDocument.getId())).willThrow(new FileNotFoundException("document does not exist"));
        assertEquals(400, facadeFileController.getPath(goodDocument.getId(), Document.class).getStatusCode(), "get path of existing document did not return 200");
    }

    @Test
    @DisplayName("Given a correct document ID, successfully return a path for a folder")
    void getPath_goodFolder_OK() throws FileNotFoundException {
        List<FileRes> path = new ArrayList<>();
        given(folderService.getPath(goodFolder.getId())).willReturn(path);
        assertEquals(200, facadeFileController.getPath(goodFolder.getId(), Folder.class).getStatusCode(), "get path of existing folder did not return 400");
    }

    @Test
    @DisplayName("Given an incorrect document ID, expect to throw a FileNotFound exception upon asking for a folder's path")
    void getPath_badFolder_BadRequest() throws FileNotFoundException {
        given(folderService.getPath(goodFolder.getId())).willThrow(new FileNotFoundException("folder does not exist"));
        assertEquals(400, facadeFileController.getPath(goodFolder.getId(), Folder.class).getStatusCode(), "get path of existing folder did not return 400");
    }

    @Test
    @DisplayName("Get all the correct parameters, and return all the files in the folder")
    void getAll_goodParameters_successfully() throws AccountNotFoundException, FileNotFoundException {
        List<Folder> folders = new ArrayList<>();
        List<Document> documents = new ArrayList<>();
        given(folderService.get(goodFolder.getId(), user.getId())).willReturn(folders);
        given(documentService.get(goodFolder.getId(), user.getId())).willReturn(documents);
        assertEquals(200, facadeFileController.getAll(goodFolder.getId(), user.getId()).getStatusCode(), "get all with good parameters did not return 200");
    }

    @Test
    @DisplayName("Given a folder that does not exist in the database, return a bad request response")
    void getAll_folderDoesNotExist_badRequest() throws AccountNotFoundException, FileNotFoundException {
        given(folderService.get(goodFolder.getId(), user.getId())).willThrow(new AccountNotFoundException("folder does not exist"));
        assertEquals(400, facadeFileController.getAll(goodFolder.getId(), user.getId()).getStatusCode(), "get all with invalid parameters did not return 400");
    }

    @Test
    @DisplayName("Given all correct inputs, successfully rename a document")
    void rename_validDocumentName_OK() throws FileNotFoundException {
        given(documentService.rename(goodDocument.getId(), goodDocument.getName())).willReturn(1);
        assertEquals(200, facadeFileController.rename(goodDocument.getId(), goodDocument.getName(), Document.class).getStatusCode());
    }

    @Test
    @DisplayName("Given an invalid name, fail to rename a document")
    void rename_invalidName_badRequest() {
        assertEquals(400, facadeFileController.rename(goodDocument.getId(), "@", Document.class).getStatusCode());
    }

    @Test
    @DisplayName("Given all correct inputs, successfully rename a folder")
    void rename_validFolderName_OK() throws FileNotFoundException {
        given(folderService.rename(goodFolder.getId(), goodFolder.getName())).willReturn(1);
        assertEquals(200, facadeFileController.rename(goodFolder.getId(), goodFolder.getName(), Folder.class).getStatusCode());
    }

    @Test
    @DisplayName("When given a correct folder, successfully delete a folder from the database")
    void delete_validFolderParameters_OK()  {
        assertEquals(200, facadeFileController.delete(goodFolder.getId(), Folder.class).getStatusCode());
    }

    @Test
    @DisplayName("When given a null ID, return a bad request")
    void delete_nullId_badRequest() {
        assertEquals(400, facadeFileController.delete(null, Folder.class).getStatusCode());
    }

    @Test
    @DisplayName("When given a valid document parameters, successfully delete a document frin the database")
    void delete_validDocumentParameters_OK()  {
        assertEquals(200, facadeFileController.delete(goodDocument.getId(), Document.class).getStatusCode());
    }

    @Test
    @DisplayName("When given all correct parameters, successfully relocate a document")
    void relocate_validDocumentParameters_OK() throws FileNotFoundException {
        given(documentService.relocate(goodFolder, goodDocument.getId())).willReturn(1);
        given(folderService.findById(goodFolder.getId())).willReturn(goodFolder);
        assertEquals(200, facadeFileController.relocate(goodFolder.getId(), goodDocument.getId(), Document.class).getStatusCode());
    }

    @Test
    @DisplayName("When given all correct parameters, successfully relocate a folder")
    void relocate_validFolderParameters_OK() throws FileNotFoundException {
        given(folderService.findById(goodFolder.getId())).willReturn(goodFolder);
        assertEquals(200, facadeFileController.relocate(goodFolder.getId(), goodDocument.getId(), Document.class).getStatusCode());
    }

    @Test
    @DisplayName("Given invalid parent folder when trying to relocate a folder, throws an exception")
    void relocate_invalidParentFolder_OK() throws FileNotFoundException {
        given(folderService.findById(goodFolder.getId())).willThrow(new FileNotFoundException("this folder does not exist"));
        assertEquals(400, facadeFileController.relocate(goodFolder.getId(), goodDocument.getId(), Document.class).getStatusCode());
    }

    @Test
    @DisplayName("Given invalid parent folder when trying to relocate a document, throws an exception")
    void export_validParameters_OK() throws FileNotFoundException {
        given(documentService.findById(goodDocument.getId())).willReturn(goodDocument);
        assertEquals(200, facadeFileController.export(goodDocument.getId()).getStatusCode());
    }

    @Test
    @DisplayName("Trying to export a document with a wrong document ID, leads to an exception throw")
    void export_invalidParameters_badRequest() throws FileNotFoundException {
        given(documentService.findById(goodDocument.getId())).willThrow(new FileNotFoundException("document does not exist"));
        assertEquals(400, facadeFileController.export(goodDocument.getId()).getStatusCode());
    }

    @Test
    @DisplayName("When checking if a document exists, upon given correct document ID, return a correct response")
    void doesExist_ExistDocument_OK()  {
        given(documentService.doesExist(goodDocument.getId())).willReturn(true);
        assertEquals(200, facadeFileController.doesExist(goodDocument.getId(), Document.class).getStatusCode());
    }

    @Test
    @DisplayName("When checking if a document exists, upon given an incorrect document ID, return a bad response")
    void doesExist_NotExistDocument_OK()  {
        given(documentService.doesExist(goodDocument.getId())).willReturn(false);
        assertEquals(200, facadeFileController.doesExist(goodDocument.getId(), Document.class).getStatusCode());
    }

    @Test
    @DisplayName("When checking if a folder exists, upon given correct folder ID, return a correct response")
    void doesExist_ExistFolder_OK()  {
        given(folderService.doesExist(goodFolder.getId())).willReturn(true);
        assertEquals(200, facadeFileController.doesExist(goodFolder.getId(), Folder.class).getStatusCode());
    }

    @Test
    @DisplayName("When checking if a folder exists, upon given an incorrect folder ID, return a bad response")
    void doesExist_NotExistFolder_OK()  {
        given(folderService.doesExist(goodFolder.getId())).willReturn(false);
        assertEquals(200, facadeFileController.doesExist(goodFolder.getId(), Folder.class).getStatusCode());
    }

    @Test
    @DisplayName("When trying to get a file's content, given a valid document ID, successfully return it's content")
    void getContent_validDocument_OK() throws FileNotFoundException {
        given(documentService.getContent(goodDocument.getId())).willReturn("some content");
        assertEquals(200, facadeFileController.getContent(goodDocument.getId()).getStatusCode());
    }

    @Test
    @DisplayName("When trying to get a file's content, given an invalid document ID, fail to return it's content")
    void getContent_invalidDocument_badRequest() throws FileNotFoundException {
        given(documentService.getContent(goodDocument.getId())).willThrow(new FileNotFoundException("document does not exist"));
        assertEquals(400, facadeFileController.getContent(goodDocument.getId()).getStatusCode());
    }

    @Test
    @DisplayName("When trying to get a document's name, given a valid document ID, successfully return it's name")
    void getDocumentName_validDocument_OK() throws FileNotFoundException {
        given(documentService.findById(goodDocument.getId())).willReturn(goodDocument);
        assertEquals(200, facadeFileController.getDocumentName(goodDocument.getId()).getStatusCode());
    }

    @Test
    @DisplayName("When trying to get a document's name, given an incorrect document ID, return a bad request")
    void getDocumentName_invalidDocument_badRequest() throws FileNotFoundException {
        given(documentService.findById(goodDocument.getId())).willThrow(new FileNotFoundException("document does not exist"));
        assertEquals(404, facadeFileController.getDocumentName(goodDocument.getId()).getStatusCode());
    }
}




