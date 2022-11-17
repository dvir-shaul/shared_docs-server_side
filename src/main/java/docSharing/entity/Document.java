package docSharing.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "document")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private LocalDate creationDate;
    private Long userId;
    private Long folderId;
    private String title;
    private Boolean isPrivate;
    private String content;

    private Document() {
        this.isPrivate = true;
        this.creationDate = LocalDate.now();
    }

    public static Document createDocument(Long userId, String title, Long folderId) {
        Document doc = new Document();
        doc.setTitle(title);
        doc.setFolderId(folderId);
        doc.setUserId(userId);
        return doc;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id) && Objects.equals(creationDate, document.creationDate) && Objects.equals(userId, document.userId) && Objects.equals(folderId, document.folderId) && Objects.equals(title, document.title) && Objects.equals(isPrivate, document.isPrivate) && Objects.equals(content, document.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creationDate, userId, folderId, title, isPrivate, content);
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", creationDate=" + creationDate +
                ", folderId=" + folderId +
                ", title='" + title + '\'' +
                ", isPrivate=" + isPrivate +
                ", content='" + content + '\'' +
                '}';
    }
}
