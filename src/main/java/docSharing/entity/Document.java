package docSharing.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import docSharing.utils.ExceptionMessage;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "Document")
@Table(name = "documents")
public class Document extends GeneralItem {

    private Boolean isPrivate;
    @Column(name = "content", columnDefinition = "text")
    private String content;
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Log> logs=new HashSet<>();

    public Set<Log> getLogs() {
        return logs;
    }

    public void setLogs(Set<Log> logs) {
        this.logs = logs;
    }

   public Document() {
        super();
        this.isPrivate = true;
    }

    public void addLog(Log log) {
        this.logs.add(log);
    }

    public static Document createDocument(User user, String name, Folder folder, String content) {
        Document doc = new Document();
        doc.setName(name);
        doc.setParentFolder(folder);
        doc.setUser(user);
        doc.setContent(content == null ? "" : content);
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
        if (content == null) throw new IllegalArgumentException(ExceptionMessage.NULL_INPUT.toString());
        this.content = content;
    }

}
