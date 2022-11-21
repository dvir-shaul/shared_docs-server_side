package docSharing.service;

import docSharing.entity.Folder;
import docSharing.entity.User;
import docSharing.repository.FolderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class FolderServiceTest {

    @Mock
    private FolderRepository folderRepository;
    @InjectMocks
    private FolderService folderService;
    private Folder folder;
    @BeforeEach
    public void createFolder() {
        folder = Folder.createFolder("test",1l,1l);
    }
    @Test
    public void givenFolderObject_whenCreateFolder_thenReturnFolderId() {
        given(folderRepository.findById(folder.getId())).willReturn(null);
        given(folderRepository.save(folder)).willReturn(folder);
       // assertThat(folderService.create(folder)).isEqualTo(folder.getId());
    }
}
