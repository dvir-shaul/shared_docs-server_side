package docSharing.service;

import docSharing.entity.Folder;
import docSharing.entity.User;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserRepository;
import docSharing.response.FileRes;
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

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class FolderServiceTest {

    @Mock
    private FolderRepository folderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DocumentService documentService;
    @InjectMocks
    private FolderService folderService;

    private Folder folder;
    private Folder parentFolder;
    private User user;

    @BeforeEach
    public void setUp() {
        user = User.createUser("asaf396@gmai.com", "dvir1234", "dvir");
        user.setId(1l);
        user.setActivated(true);
        parentFolder = Folder.createFolder("parent", null, user);
        parentFolder.setId(2L);
        folder = Folder.createFolder("test", parentFolder, user);
        folder.setId(1L);
    }


    @Test
    @DisplayName("Make sure a folder is returned when calling findById with valid parameters")
    void findById_givenCorrectUser_returnsUserSuccessfully() throws FileNotFoundException {
        given(folderRepository.findById(1L)).willReturn(Optional.of(folder));
        assertEquals(folder, folderService.findById(1L));
    }

    @Test
    @DisplayName("Make sure an exception is thrown when calling findById with invalid parameters")
    void findById_givenWrongUser_throwsException() {
        given(folderRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> folderService.findById(1L));
    }

    @Test
    @DisplayName("Make sure an exception is thrown when calling get when folder does not exist")
    void get_givenWrongInformation_throwFileNotFoundException() {
        given(folderRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> folderService.get(1L, 1L));
    }

    @Test
    @DisplayName("Make sure an exception is thrown when calling get when user does not exist")
    void get_givenWrongInformation_throwAccountNotFound() {
        given(folderRepository.findById(1L)).willReturn(Optional.of(folder));
        given(userRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> folderService.get(1L, 1L));
    }

    @Test
    @DisplayName("Make sure a list of folders is returned when calling get with valid parameters")
    void get_givenCorrectInformation_returnListOfFolders() throws FileNotFoundException, AccountNotFoundException {
        List<Folder> folderList = new ArrayList<>();
        given(folderRepository.findById(1L)).willReturn(Optional.of(folder));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(folderRepository.findAllByParentFolderIdAndUserId(folder, user)).willReturn(folderList);
        assertEquals(folderList.getClass(), folderService.get(1L, 1L).getClass());
    }

    @Test
    @DisplayName("Make sure a list of folders is returned when calling getAllWhereParentFolderIsNull with valid parameters")
    void getAllWhereParentFolderIsNull_givenCorrectUserId_returnsListOfFolders() throws AccountNotFoundException {
        List<Folder> folderList = new ArrayList<>();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(folderRepository.findAllByParentFolderIsNull(user)).willReturn(folderList);
        assertEquals(folderList.getClass(), folderService.getAllWhereParentFolderIsNull(1L).getClass());
    }

    @Test
    @DisplayName("Make sure an exception is thrown when calling getAllWhereParentFolderIsNull with invalid parameters")
    void getAllWhereParentFolderIsNull_givenWrongUserId_throwsAccountNotFoundException() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> folderService.getAllWhereParentFolderIsNull(1L));
    }

    @Test
    @DisplayName("Make sure an exception is thrown when calling create with null parent folder")
    void create_WithIncorrectParentFolder_ThrowsFileNotFoundException() {
        given(folderRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> folderService.create(folder, user, "hey", null));
    }


    @Test
    @DisplayName("Make sure a path of folder is returned when calling getPath with valid parameters")
    void getPath_withLegalArgument_returnList() throws FileNotFoundException {
        List<FileRes> files = new ArrayList<>();
        given(folderRepository.findById(1L)).willReturn(Optional.of(folder));
        assertEquals(files.getClass(), folderService.getPath(1L).getClass());
    }

    @Test
    @DisplayName("Make sure an exception is thrown when calling getPath with invalid parameters")
    void getPath_withIllegalArguments_throwsFileNotFoundException() {
        given(folderRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> folderService.getPath(1L));
    }

    @Test
    @DisplayName("Make sure an exception is thrown when calling rename with folder that does not exist")
    void rename_withUnrecognizedFolderId_throwFileNotFoundException() {
        given(folderRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> folderService.rename(1L, "hey"));
    }

    @Test
    @DisplayName("Make sure 1 is returned when calling rename with valid folder")
    void rename_withRecognizedFolderId_returnsInt1() throws FileNotFoundException {
        given(folderRepository.findById(1L)).willReturn(Optional.of(folder));
        given(folderRepository.updateName("hey", 1L)).willReturn(1);
        assertEquals(1, folderService.rename(1L, "hey"));
    }


    @Test
    @DisplayName("Make sure 1 is returned when calling delete with valid folder")
    void delete_realFolder_success() throws FileNotFoundException {
        given(folderRepository.findById(1L)).willReturn(Optional.of(folder));
        when(documentService.delete(2L)).thenReturn(1);
        assertEquals(1, folderService.delete(1L));
    }

    @Test
    @DisplayName("Make sure an exception is thrown when calling rename with invalid folder")
    void delete_fakeFolderId_fail(){
        given(folderRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> folderService.delete(1L));
    }

    @Test
    @DisplayName("Make sure root folders being created when calling createRootFolders with valid parameters")
    public void createRootFolders_validUser_createsFolders() {
        Folder general = Folder.createFolder("General", null, user);
        Folder personal = Folder.createFolder("Personal", null, user);
        Folder programming = Folder.createFolder("Programming", null, user);
        Folder design = Folder.createFolder("Design", null, user);
        Folder business = Folder.createFolder("Business", null, user);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(folderRepository.save(general)).willReturn(general);
        given(folderRepository.save(personal)).willReturn(personal);
        given(folderRepository.save(programming)).willReturn(programming);
        given(folderRepository.save(design)).willReturn(design);
        given(folderRepository.save(business)).willReturn(business);
        assertDoesNotThrow(() -> folderService.createRootFolders(user));
    }

    @Test
    @DisplayName("Make sure exception is being thrown when calling createRootFolders with user that does not exist")
    public void createRootFolders_userDoesNotExist_throwsException() throws AccountNotFoundException {
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> folderService.createRootFolders(user));
    }

    @Test
    @DisplayName("Make sure ture is returned when calling doesExist with existing folder")
    public void doesExist_folderExist_returnsTrue() {
        given(folderRepository.findById(folder.getId())).willReturn(Optional.of(folder));
        assertTrue(folderService.doesExist(folder.getId()));
    }
    @Test
    @DisplayName("Make sure false is returned when calling doesExist with non-existing folder")
    public void doesExist_folderDoesNotExist_returnsFalse() {
        given(folderRepository.findById(folder.getId())).willReturn(Optional.empty());
        assertFalse(folderService.doesExist(folder.getId()));
    }
    @Test
    @DisplayName("Make sure false is returned when calling relocate with non-existing folder")
    public void relocate_folderDoesNotExist_returnsFalse() {
        given(folderRepository.findById(folder.getId())).willReturn(Optional.empty());
        assertFalse(folderService.doesExist(folder.getId()));
    }
}
