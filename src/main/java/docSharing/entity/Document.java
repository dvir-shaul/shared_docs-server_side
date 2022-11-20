package docSharing.entity;

import javax.persistence.*;

@Entity(name = "Document")
@Table(name = "document")
public class Document extends GeneralItem {

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

    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return super.toString() + "Document{" +
                "isPrivate=" + isPrivate +
                ", content='" + content + '\'' +
                '}';
    }
}
