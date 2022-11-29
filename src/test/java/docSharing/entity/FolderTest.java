package docSharing.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FolderTest {
    private Folder parentFolder;
    private User user;
    @BeforeEach
    public void setup(){
        parentFolder = Folder.createFolder("parent", null);
        user = User.createUser("test@test.com", "Abcd1234", "test");
    }

    @Test
    public void createFolder_goodParameters_folderCreated() {
        Folder folder = Folder.createFolder("test", parentFolder, user);
        assertNotNull(folder, "The folder was not created");
    }

    @Test
    public void createFolder_goodParameters_testName() {
        Folder folder = Folder.createFolder("test", parentFolder, user);
        assertEquals(folder.getName(), "test", "folder's name is not correct");
    }

    @Test
    public void createFolder_goodParameters_testParentFolderId() {
        Folder folder = Folder.createFolder("test", parentFolder);
        assertEquals(folder.getParentFolder(), parentFolder, "folder's parent folder id is not correct");
    }

    @Test
    public void createFolder_goodParameters_testId() {
        Folder folder = Folder.createFolder("test", parentFolder, user);
        assertEquals(folder.getUser(), user, "folder's user id is not correct");
    }

    @Test
    public void createFolder_nullName_exceptionThrown() {
        assertThrows(IllegalArgumentException.class, () -> Folder.createFolder(null, null), "created folder with null name");
    }

    @Test
    public void createFolder_emptyName_exceptionThrown() {
        assertThrows(IllegalArgumentException.class, () -> Folder.createFolder("", null), "created folder with null name");
    }

    @Test
    public void createFolder_nullId_exceptionThrown() {
        assertThrows(IllegalArgumentException.class, () -> Folder.createFolder("test", parentFolder, null), "created folder with null user id");
    }
}
