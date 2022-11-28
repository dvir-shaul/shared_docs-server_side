package docSharing.entity;

import docSharing.utils.ExceptionMessage;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(name = "Document")
@Table(name = "documents")
public class Document extends GeneralItem {

    private Boolean isPrivate;
    @Column(name = "content", columnDefinition = "text")
    private String content;


    private Document() {
        super();
        this.isPrivate = true;
    }

    public static Document createDocument(String name, Folder parentFolder) {
        Document document = new Document();
        document.setName(name);
        document.setParentFolder(parentFolder);
        return document;
    }

    public static Document createDocument(User user, String name, Folder folder) {
        Document doc = new Document();
        doc.setName(name);
        doc.setParentFolder(folder);
        doc.setContent("");
        doc.setUser(user);
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
