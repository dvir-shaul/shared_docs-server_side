//package docSharing.entity;
//
//import docSharing.repository.DocumentRepository;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.time.LocalDate;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class GeneralItemTest {
//
//    private static GeneralItem item;
//    @Autowired
//    private DocumentRepository documentRepository;
//
//    @BeforeEach
//    void setup(){
//        item = new GeneralItem();
//    }
//    @AfterEach
//    void reset(){
//
//    }
//
//
//    @Test
//    void getId_baseValueNull_assertNull() {
//        assertNull(item.getId());
//    }
//
//    @Test
//    void setId() {
//        item.setId(2L);
//        assertEquals(item.getId(),2L);
//    }
//
//    @Test
//    void getCreationDate() {
//        LocalDate ld = item.getCreationDate();
//        assertEquals(ld,item.getCreationDate());
//    }
//
//    @Test
//    void setCreationDate() {
//        LocalDate ld = item.getCreationDate();
//        item.setCreationDate(LocalDate.of(2022,11,19));
//        assertNotEquals(item.getCreationDate(),ld);
//    }
//
//    @Test
//    void getUserId_baseValueNull_assertNull() {
//        assertNull(item.getUserId());
//
//    }
//
//    @Test
//    void generalItem_setUserIDNull_throwsException() {
//        assertThrows(IllegalArgumentException.class, () ->  item.setUserId(null), "set content with null value did not throw the correct exception");
//    }
//
//    @Test
//    void getParentFolderId_baseValueNull_assertNull() {
//        assertNull(item.getParentFolderId());
//
//    }
//
//    @Test
//    void setParentFolderId_toRegularValue_assertEqualsAsValue() {
//        item.setParentFolderId(1L);
//        assertEquals(item.getParentFolderId(),1L);
//    }
//
//    @Test
//    void getName() {
//        assertNull(item.getName());
//    }
//
//    @Test
//    void setName() {
//        item.setName("Hernan");
//        assertEquals(item.getName(),"Hernan");
//    }
//
//    @Test
//    void testToString_True() {
//        String string = item.toString();
//        assertEquals(string,item.toString());
//    }
//    @Test
//    void testToString_changeValue_AssertNE() {
//        String string = item.toString();
//        item.setName("Gomez");
//        assertNotEquals(string,item.toString());
//    }
//}