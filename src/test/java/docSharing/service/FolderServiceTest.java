package docSharing.service;

import docSharing.entity.Folder;
import docSharing.entity.User;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserRepository;
import docSharing.response.FileRes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.matchers.Null;
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

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class FolderServiceTest {

    @Mock
    private FolderRepository folderRepository;
    @Mock
    private UserRepository userRepository;
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
        folder = Folder.createFolder("test", parentFolder, user);
        folder.setId(1L);
    }
//    @Test
//    void givenFolderObject_whenCreateFolder_thenReturnFolderId() {
//        given(folderRepository.findById(folder.getId())).willReturn(null);
//        given(folderRepository.save(folder)).willReturn(folder);
//       // assertThat(folderService.create(folder)).isEqualTo(folder.getId());
//    }

//    @Test
//    void getActiveUsers_givenCorrectInformationWithGetMethod_returnUsersList() {
//
//        given(userRepository.findById(1L)).willReturn(Optional.of(user));
//        assertEquals(HashSet.class, documentService);
//    }

    @Test
    void findById_givenCorrectUser_returnsUserSucessfully() throws FileNotFoundException {
        given(folderRepository.findById(1L)).willReturn(Optional.of(folder));
        assertEquals(folder, folderService.findById(1L));
    }

    @Test
    void findById_givenWrongUser_throwsException() {
        given(folderRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> folderService.findById(1L));
    }

    @Test
    void get_givenWrongInformation_throwFileNotFoundException() {
        given(folderRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> folderService.get(1L, 1L));
    }

    @Test
    void get_givenWrongInformation_throwAccountNotFound() {
        given(folderRepository.findById(1L)).willReturn(Optional.of(folder));
        given(userRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> folderService.get(1L, 1L));
    }

    @Test
    void get_givenCorrectInformation_returnListOfFolders() throws FileNotFoundException, AccountNotFoundException {
        List<Folder> folderList = new ArrayList<>();
        given(folderRepository.findById(1L)).willReturn(Optional.of(folder));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(folderRepository.findAllByParentFolderIdAndUserId(folder, user)).willReturn(folderList);
        assertEquals(folderList.getClass(), folderService.get(1L, 1L).getClass());
    }

    @Test
    void getAllWhereParentFolderIsNull_givenCorrectUserId_returnsListOfFolders() throws AccountNotFoundException {
        List<Folder> folderList = new ArrayList<>();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(folderRepository.findAllByParentFolderIsNull(user)).willReturn(folderList);
        assertEquals(folderList.getClass(), folderService.getAllWhereParentFolderIsNull(1L).getClass());
    }

    @Test
    void getAllWhereParentFolderIsNull_givenWrongUserId_throwsAccountNotFoundException() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> folderService.getAllWhereParentFolderIsNull(1L));
    }

    @Test
    void create_WithIncorrectParentFolder_ThrowsFileNotFoundException() {
        given(folderRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> folderService.create(folder, user, "hey", null));
    }

//    @Test
//    void create_withCorrectParameters_returnFolderSuccessfully() throws FileNotFoundException {
//        given(folderRepository.findById(1L)).willReturn(Optional.of(parentFolder));
//        given(folderRepository.save(folder)).willReturn(folder);
//        assertEquals(folder, folderService.create(folder, user, "hey", null));
//    }

    @Test
    void getPath_withLegalArgument_returnList() throws FileNotFoundException {
        List<FileRes> files = new ArrayList<>();
        given(folderRepository.findById(1L)).willReturn(Optional.of(folder));
        assertEquals(files.getClass(), folderService.getPath(1L).getClass());
    }

    @Test
    void getPath_withIllegalArguments_throwsFileNotFoundException() {
        given(folderRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> folderService.getPath(1L));
    }

    @Test
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
    public void createRootFolders_null_throwsException() throws AccountNotFoundException {
        assertThrows(NullPointerException.class, () -> folderService.createRootFolders(null));
    }

    @Test
    public void createRootFolders_userDoesNotExist_throwsException() throws AccountNotFoundException {
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> folderService.createRootFolders(user));
    }

    @Test
    public void doesExist_folderExist_returnsTrue() {
        given(folderRepository.findById(folder.getId())).willReturn(Optional.of(folder));
        assertTrue(folderService.doesExist(folder.getId()));
    }
    @Test
    public void doesExist_folderDoesNotExist_returnsFalse() {
        given(folderRepository.findById(folder.getId())).willReturn(Optional.empty());
        assertFalse(folderService.doesExist(folder.getId()));
    }
    @Test
    public void relocate_folderDoesNotExist_returnsFalse() {
        given(folderRepository.findById(folder.getId())).willReturn(Optional.empty());
        assertFalse(folderService.doesExist(folder.getId()));
    }
}