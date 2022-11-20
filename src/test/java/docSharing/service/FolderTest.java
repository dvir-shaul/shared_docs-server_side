package docSharing.service;

import docSharing.entity.Folder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FolderTest {

    @Test
    public void createFolder_goodParameters_folderCreated(){
        Folder folder=Folder.createFolder("test",1l,1l);
        assertNotNull(folder,"The folder was not created");
    }
    @Test
    public void createFolder_goodParameters_testName(){
        Folder folder=Folder.createFolder("test",1l,1l);
        assertEquals(folder.getName(),"test","folder's name is not correct");
    }
    @Test
    public void createFolder_goodParameters_testParentFolderId(){
        Folder folder=Folder.createFolder("test",1l,1l);
        assertEquals(folder.getParentFolderId(),1l,"folder's parent folder id is not correct");
    }
    @Test
    public void createFolder_goodParameters_testId(){
        Folder folder=Folder.createFolder("test",1l,1l);
        assertEquals(folder.getUserId(),1l,"folder's user id is not correct");
    }
    @Test
    public void createFolder_nullName_exceptionThrown(){
        assertThrows(IllegalArgumentException.class, ()->Folder.createFolder(null,1l,1l),"created folder with null name");
    }
    @Test
    public void createFolder_emptyName_exceptionThrown(){
        assertThrows(IllegalArgumentException.class, ()->Folder.createFolder("",1l,1l),"created folder with null name");
    }
    @Test
    public void createFolder_nullId_exceptionThrown(){
        assertThrows(IllegalArgumentException.class, ()->Folder.createFolder("test",1l,null),"created folder with null user id");
    }
}
