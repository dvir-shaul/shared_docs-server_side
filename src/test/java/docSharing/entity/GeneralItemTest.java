package docSharing.entity;

import docSharing.repository.DocumentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class GeneralItemTest {

    private static GeneralItem item;


    @BeforeEach
    void setup(){
        item = new GeneralItem();
    }



    @Test
    void getId_baseValueNull_assertNull() {
        assertNull(item.getId());
    }

    @Test
    void setId() {
        item.setId(2L);
        assertEquals(item.getId(),2L);
    }

    @Test
    void getCreationDate() {
        LocalDate ld = item.getCreationDate();
        assertEquals(ld,item.getCreationDate());
    }

    @Test
    void setCreationDate() {
        LocalDate ld = item.getCreationDate();
        item.setCreationDate(LocalDate.of(2022,11,19));
        assertNotEquals(item.getCreationDate(),ld);
    }

    @Test
    void getUserId_baseValueNull_assertNull() {
        assertNull(item.getUser());

    }

    @Test
    void generalItem_setUserNull_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->  item.setUser(null), "set content with null value did not throw the correct exception");
    }

    @Test
    void getParentFolderId_baseValueNull_assertNull() {
        assertNull(item.getParentFolder());

    }

    @Test
    void setParentFolderId_toRegularValue_assertEqualsAsValue() {
        Folder folder=new Folder();
        item.setParentFolder(folder);
        assertEquals(item.getParentFolder(),folder);
    }

    @Test
    void getName() {
        assertNull(item.getName());
    }

    @Test
    void setName() {
        item.setName("Hernan");
        assertEquals(item.getName(),"Hernan");
    }


}