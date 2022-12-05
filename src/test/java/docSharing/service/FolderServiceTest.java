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
//    @Test
//    public void givenFolderObject_whenCreateFolder_thenReturnFolderId() {
//        given(folderRepository.findById(folder.getId())).willReturn(null);
//        given(folderRepository.save(folder)).willReturn(folder);
//       // assertThat(folderService.create(folder)).isEqualTo(folder.getId());
//    }
//}

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
    void rename_withUnrecognizedFolderId_throwFileNotFoundException() {
        given(folderRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> folderService.rename(1L, "hey"));
    }

    @Test
    void rename_withRecognizedFolderId_returnsInt1() throws FileNotFoundException {
        given(folderRepository.findById(1L)).willReturn(Optional.of(folder));
        given(folderRepository.updateName("hey", 1L)).willReturn(1);
        assertEquals(1, folderService.rename(1L, "hey"));
    }

//    @Test
//    void relocate_givenCorrectInput_willChangeParentFolder() throws FileNotFoundException {
//        given(folderRepository.findById(1L)).willReturn(Optional.of(folder));
//        given(folderRepository.updateParentFolderId(parentFolder, 1L)).willReturn(1);
//        assertEquals(1, folderService.relocate(parentFolder, 1L));
//    }

    @Test
    void delete_realFolder_success() throws FileNotFoundException {
        given(folderRepository.findById(1L)).willReturn(Optional.of(folder));
        when(documentService.delete(2L)).thenReturn(1);
        assertEquals(1, folderService.delete(1L));
    }

    @Test
    void delete_fakeFolderId_fail(){
        given(folderRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> folderService.delete(1L));
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
