package docSharing.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "document")
public class Document extends GeneralItem{

    private Boolean isPrivate;
    private String content;

    private Document() {
        super();
        this.isPrivate = true;
    }

    public static Document createDocument(Long userId, String name, Long folderId) {
        Document doc = new Document();
        doc.setName(name);
        doc.setParentFolderId(folderId);
        doc.setUserId(userId);
        return doc;
    }

}
