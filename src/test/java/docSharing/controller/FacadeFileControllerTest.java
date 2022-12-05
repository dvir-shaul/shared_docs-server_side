package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Folder;
import docSharing.entity.User;
import docSharing.response.FileRes;
import docSharing.service.DocumentService;
import docSharing.service.FolderService;
import docSharing.service.UserService;
import org.junit.jupiter.api.BeforeEach;
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
    void createDocument_goodDocument_Successfully() throws AccountNotFoundException {
        given(userService.findById(user.getId())).willReturn(user);
        given(documentService.create(null, user, "test", null)).willReturn(goodDocument.getId());
        assertEquals(201, facadeFileController.create(null, "test", null, user.getId(), Document.class).getStatusCode(), "create document with good parameters did not return 201");
    }

    @Test
    void createDocument_userIsNotExists_BadRequest() throws AccountNotFoundException {
        given(userService.findById(user.getId())).willThrow(new AccountNotFoundException("user is not exists"));
        assertEquals(400, facadeFileController.create(null, "test", null, user.getId(), Document.class).getStatusCode(), "create document with good parameters did not return 201");
    }

    @Test
    void createDocument_parentFolderNotExists_BadRequest() throws AccountNotFoundException, FileNotFoundException {
        given(folderService.findById(goodFolder.getId())).willThrow(new FileNotFoundException("folder is not exists"));
        assertEquals(400, facadeFileController.create(goodFolder.getId(), "test", null, user.getId(), Document.class).getStatusCode(), "create document with good parameters did not return 201");
    }

    @Test
    void createFolder_goodFolder_Successfully() throws AccountNotFoundException {
        given(userService.findById(user.getId())).willReturn(user);
        given(folderService.create(null, user, "test", null)).willReturn(goodFolder.getId());
        assertEquals(201, facadeFileController.create(null, "test", null, user.getId(), Folder.class).getStatusCode(), "create document with good parameters did not return 201");
    }

    @Test
    void createFolder_userIsNotExists_BadRequest() throws AccountNotFoundException {
        given(userService.findById(user.getId())).willThrow(new AccountNotFoundException("user is not exists"));
        assertEquals(400, facadeFileController.create(null, "test", null, user.getId(), Folder.class).getStatusCode(), "create document with good parameters did not return 201");
    }

    @Test
    void createFolder_parentFolderNotExists_BadRequest() throws AccountNotFoundException, FileNotFoundException {
        given(folderService.findById(goodFolder.getId())).willThrow(new FileNotFoundException("folder is not exists"));
        assertEquals(400, facadeFileController.create(goodFolder.getId(), "test", null, user.getId(), Folder.class).getStatusCode(), "create document with good parameters did not return 201");
    }

    @Test
    void getPath_goodDocument_OK() throws FileNotFoundException {
        List<FileRes> path = new ArrayList<>();
        given(documentService.getPath(goodDocument.getId())).willReturn(path);
        assertEquals(200, facadeFileController.getPath(goodDocument.getId(), Document.class).getStatusCode(), "get path of existing document did not return 200");
    }
    @Test
    void getPath_badDocument_BadRequest() throws FileNotFoundException {
        given(documentService.getPath(goodDocument.getId())).willThrow(new FileNotFoundException("document does not exist"));
        assertEquals(400, facadeFileController.getPath(goodDocument.getId(), Document.class).getStatusCode(), "get path of existing document did not return 200");
    }
    @Test
    void getPath_goodFolder_OK() throws FileNotFoundException {
        List<FileRes> path = new ArrayList<>();
        given(folderService.getPath(goodFolder.getId())).willReturn(path);
        assertEquals(200, facadeFileController.getPath(goodFolder.getId(), Folder.class).getStatusCode(), "get path of existing folder did not return 400");
    }
    @Test
    void getPath_badFolder_BadRequest() throws FileNotFoundException {
        given(folderService.getPath(goodFolder.getId())).willThrow(new FileNotFoundException("folder does not exist"));
        assertEquals(400, facadeFileController.getPath(goodFolder.getId(), Folder.class).getStatusCode(), "get path of existing folder did not return 400");
    }
    @Test
    void getAll_goodParameters_successfully() throws AccountNotFoundException, FileNotFoundException {
        List<Folder> folders=new ArrayList<>();
        List<Document> documents=new ArrayList<>();
        given(folderService.get(goodFolder.getId(), user.getId())).willReturn(folders);
        given(documentService.get(goodFolder.getId(), user.getId())).willReturn(documents);
        assertEquals(200, facadeFileController.getAll(goodFolder.getId(), user.getId()).getStatusCode(), "get all with good parameters did not return 200");
    }

    @Test
    void getAll_folderDoesNotExist_badRequest() throws AccountNotFoundException, FileNotFoundException {
        given(folderService.get(goodFolder.getId(), user.getId())).willThrow(new AccountNotFoundException("folder does not exist"));
        assertEquals(400, facadeFileController.getAll(goodFolder.getId(), user.getId()).getStatusCode(), "get all with invalid parameters did not return 400");
    }
    @Test
    void rename_validDocumentName_OK(){
        given(documentService.rename(goodDocument.getId(), goodDocument.getName())).willReturn(1);
        assertEquals(200, facadeFileController.rename(goodDocument.getId(), goodDocument.getName(), Document.class).getStatusCode());
    }
    @Test
    void rename_invalidName_badRequest(){
        assertEquals(400, facadeFileController.rename(goodDocument.getId(), "@", Document.class).getStatusCode());
    }
    @Test
    void rename_validFolderName_OK(){
        given(folderService.rename(goodFolder.getId(), goodFolder.getName())).willReturn(1);
        assertEquals(200, facadeFileController.rename(goodFolder.getId(), goodFolder.getName(), Folder.class).getStatusCode());
    }
    @Test
    void delete_validFolderParameters_OK() throws FileNotFoundException {
        assertEquals(200, facadeFileController.delete(goodFolder.getId(), Folder.class).getStatusCode());
    }
    @Test
    void delete_nullId_badRequest() throws FileNotFoundException {
        assertEquals(400, facadeFileController.delete(null, Folder.class).getStatusCode());
    }
    @Test
    void delete_validDocumentParameters_OK() throws FileNotFoundException {
        assertEquals(200, facadeFileController.delete(goodDocument.getId(), Document.class).getStatusCode());
    }
    @Test
    void relocate_validDocumentParameters_OK() throws FileNotFoundException {
        given(documentService.relocate(goodFolder, goodDocument.getId())).willReturn(1);
        given(folderService.findById(goodFolder.getId())).willReturn(goodFolder);
        assertEquals(200, facadeFileController.relocate(goodFolder.getId(), goodDocument.getId(), Document.class).getStatusCode());
    }
    @Test
    void relocate_validFolderParameters_OK() throws FileNotFoundException {
        given(folderService.findById(goodFolder.getId())).willReturn(goodFolder);
        assertEquals(200, facadeFileController.relocate(goodFolder.getId(), goodDocument.getId(), Document.class).getStatusCode());
    }

    @Test
    void relocate_invalidParentFolder_OK() throws FileNotFoundException {
        given(folderService.findById(goodFolder.getId())).willThrow(new FileNotFoundException("this folder does not exist"));
        assertEquals(400, facadeFileController.relocate(goodFolder.getId(), goodDocument.getId(), Document.class).getStatusCode());
    }
    @Test
    void export_validParameters_OK() throws FileNotFoundException {
        given(documentService.findById(goodDocument.getId())).willReturn(goodDocument);
        assertEquals(200, facadeFileController.export(goodDocument.getId()).getStatusCode());
    }
    @Test
    void export_invalidParameters_badRequest() throws FileNotFoundException {
        given(documentService.findById(goodDocument.getId())).willThrow(new FileNotFoundException("document does not exist"));
        assertEquals(400, facadeFileController.export(goodDocument.getId()).getStatusCode());
    }
    @Test
    void doesExist_ExistDocument_OK() throws FileNotFoundException {
        given(documentService.doesExist(goodDocument.getId())).willReturn(true);
        assertEquals(200, facadeFileController.doesExist(goodDocument.getId(), Document.class).getStatusCode());
    }
    @Test
    void doesExist_NotExistDocument_OK() throws FileNotFoundException {
        given(documentService.doesExist(goodDocument.getId())).willReturn(false);
        assertEquals(200, facadeFileController.doesExist(goodDocument.getId(), Document.class).getStatusCode());
    }
    @Test
    void doesExist_ExistFolder_OK() throws FileNotFoundException {
        given(folderService.doesExist(goodFolder.getId())).willReturn(true);
        assertEquals(200, facadeFileController.doesExist(goodFolder.getId(), Folder.class).getStatusCode());
    }
    @Test
    void doesExist_NotExistFolder_OK() throws FileNotFoundException {
        given(folderService.doesExist(goodFolder.getId())).willReturn(false);
        assertEquals(200, facadeFileController.doesExist(goodFolder.getId(), Folder.class).getStatusCode());
    }

    @Test
    void getContent_validDocument_OK() throws FileNotFoundException {
        given(documentService.getContent(goodDocument.getId())).willReturn("some content");
        assertEquals(200, facadeFileController.getContent(goodDocument.getId()).getStatusCode());
    }
    @Test
    void getContent_invalidDocument_badRequest() throws FileNotFoundException {
        given(documentService.getContent(goodDocument.getId())).willThrow(new FileNotFoundException("document does not exist"));
        assertEquals(400, facadeFileController.getContent(goodDocument.getId()).getStatusCode());
    }
    @Test
    void getDocumentName_validDocument_OK() throws FileNotFoundException {
        given(documentService.findById(goodDocument.getId())).willReturn(goodDocument);
        assertEquals(200, facadeFileController.getDocumentName(goodDocument.getId()).getStatusCode());
    }
    @Test
    void getDocumentName_invalidDocument_badRequest() throws FileNotFoundException {
        given(documentService.findById(goodDocument.getId())).willThrow(new FileNotFoundException("document does not exist"));
        assertEquals(404, facadeFileController.getDocumentName(goodDocument.getId()).getStatusCode());
    }

}




