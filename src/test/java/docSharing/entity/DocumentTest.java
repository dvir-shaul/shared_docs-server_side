package docSharing.entity;

import docSharing.repository.DocumentRepository;
import org.junit.jupiter.api.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
class DocumentTest {
    private static Document doc;
    @Autowired
    private DocumentRepository documentRepository;

    @BeforeEach
    void setup(){
        doc = Document.createDocument(2L,"docs", 1L);
    }
    @AfterEach
    void reset(){
        List<Document> documentList = documentRepository.findAll();
        for (Document document : documentList) {
            documentRepository.deleteById(document.getId());
        }
    }

    @Test
    void createDocument_GoodValues_successfulCreation() {
        Document doc2 = Document.createDocument(2L,"docs", 1L);
        assertEquals(doc.toString(),doc2.toString());
    }
    @Test
    void createDocument_setUserIDNull_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->  Document.createDocument(null,"docs", 1L), "set content with null value did not throw the correct exception");
    }
    @Test
    void createDocument_setDocsNameNull_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->  Document.createDocument(2L,null, 1L), "set content with null value did not throw the correct exception");
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
    void getContent_EmptyDocument_AssertNull() {
        assertNull(doc.getContent());
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

    @Test
    void testToString_withChangeContent_assertNotEquals() {
        String a = doc.toString();
        doc.setPrivate(false);
        assertNotEquals(a,doc.toString());
    }
}