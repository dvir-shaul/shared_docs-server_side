package docSharing.entity;

import docSharing.repository.DocumentRepository;
import org.junit.jupiter.api.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;


import javax.print.Doc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;



class DocumentTest {
    private static Document doc;
    private static User user;
    private static Folder parentFolder;
    @Autowired
    private DocumentRepository documentRepository;

    @BeforeEach
    void setup(){
        user = User.createUser("test@test.com", "Abcd1234", "test");
        parentFolder = Folder.createFolder("parent", null);
        doc = Document.createDocument(user,"docs", parentFolder,"");

    }



    @Test
    void createDocument_GoodValues_successfulCreation() {
        assertNotNull(doc, "The document was not created");
    }
    @Test
    void createDocument_setUserNull_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->  Document.createDocument(null,"docs", parentFolder,""), "set content with null value did not throw the correct exception");
    }
    @Test
    void createDocument_setDocsNameNull_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->  Document.createDocument(user,null, parentFolder,""), "set content with null value did not throw the correct exception");
    }


    @Test
    void getPrivate_fromBaseValue_assertTrue() {
        assertEquals(true,doc.getPrivate());
    }
    @Test
    void setPrivate_fromTrue_ToTrue() {
        assertEquals(true,doc.getPrivate());
        doc.setPrivate(true);
        assertEquals(true,doc.getPrivate());
    }

    @Test
    void setPrivate_fromTrue_ToFalse() {
        assertEquals(true,doc.getPrivate());
        doc.setPrivate(false);
        assertEquals(false,doc.getPrivate());
    }

    @Test
    void getContent_EmptyDocument_AssertEmpty() {
        assertEquals(doc.getContent(),"","the content is not equals to actual content");
    }

    @Test
    void setContent_newString() {
        doc.setContent("hey this is moses");
        assertEquals(doc.getContent(),"hey this is moses");
    }

    @Test
    void setContent_NullString_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->  doc.setContent(null), "set content with null value did not throw the correct exception");
    }

}