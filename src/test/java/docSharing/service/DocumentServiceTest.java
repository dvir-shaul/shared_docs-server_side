package docSharing.service;


import docSharing.entity.Document;
import docSharing.entity.Folder;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {


    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private FolderRepository folderRepository;

    @InjectMocks
    private DocumentService documentService;
    @InjectMocks
    private FolderService folderService;

    private Document document;
    private Folder folder;

    @BeforeEach
    public void createFolder() {
        folder = Folder.createFolder("test", 1L, 1L);
        System.out.println(folder);
        folderRepository.save(folder);
    }

    @Test
    void create_SuccessfulCreation_Document() {
        assertNotNull(documentRepository);
        assertNotNull(folderRepository);
        assertNotNull(documentService);
        assertNotNull(folderService);
        folder = Folder.createFolder("test", 1L, 1L);
        folder.setId(10L);
        folderRepository.save(folder);
        verify(folderRepository,atLeastOnce());
        given(folderRepository.save(folder)).willReturn(folder);
        System.out.println(folderRepository.findAll());
        document = Document.createDocument(1L,"My first Document", folder.getId());
        document.setId(2L);
        document.setContent("howwwwwwwwwwwwwwwwwww");
        System.out.println(document);
//        given(documentService.create(document)).willReturn(document.getId());

        given(documentRepository.save(document)).willReturn(document);

        System.out.println(documentRepository.getReferenceById(2L));


    }
    @Test
    void create_FailedCreationOfDocument_throwsException() {

    }
    @Test
    void rename_SuccessfulRename_Document() {
    }
    @Test
    void rename_FailedRenameDocument_throwsException() {
    }
    @Test
    void relocate_SuccessfulRelocate_Document() {
    }

    @Test
    void relocate_FailedRelocateDocument_throwsException() {
    }

    @Test
    void delete_SuccessfulDelete_Document() {
    }

    @Test
    void delete_FailedRDeleteDocument_throwsException() {
    }

    @Test
    void create_SuccessfulCreation_Folder() {

    }
    @Test
    void create_FailedCreationOfFolder_throwsException() {

    }
    @Test
    void rename_SuccessfulRename_Folder() {
    }

    @Test
    void rename_FailedRenameFolder_throwsException() {
    }

    @Test
    void relocate_SuccessfulRelocate_Folder() {
    }

    @Test
    void relocate_FailedRelocateFolder_throwsException() {
    }
    @Test
    void delete_SuccessfulDelete_Folder() {
    }

    @Test
    void delete_FailedDeleteFolder_throwsException() {
    }

}