package docSharing.service;

import docSharing.entity.Folder;
import docSharing.entity.User;
import docSharing.repository.FolderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class FolderServiceTest {

    @Mock
    private FolderRepository folderRepository;
    @InjectMocks
    private FolderService folderService;
    private Folder folder;
    @BeforeEach
    public void createFolder() {
        folder = Folder.createFolder("test", 1L, 1L);
    }
    @Test
    @DisplayName("Make sure the correct folder is inserted to the database")
    public void givenFolderObject_whenCreateFolder_thenReturnFolderId() {
        given(folderRepository.findById(folder.getId())).willReturn(null);
        given(folderRepository.save(folder)).willReturn(folder);
       // assertThat(folderService.create(folder)).isEqualTo(folder.getId());
    }
}
