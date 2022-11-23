package docSharing.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "Folder")
@Table(name = "folders")
public class Folder extends GeneralItem {
    @OneToMany(mappedBy = "parentFolder", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Folder> folders = new HashSet<>();
    @OneToMany(mappedBy = "parentFolder", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Document> documents = new HashSet<>();

    //    private Folder() {
//        super();
//    }
    public Folder() {
        super();
    }

    ;

    public Set<Folder> getFolders() {
        return folders;
    }

    public void setFolders(Set<Folder> folders) {
        this.folders = folders;
    }

    public Set<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(Set<Document> documents) {
        this.documents = documents;
    }

    public void removeFolder(Folder folder) {
        this.folders.remove(folder);
    }


    public void removeDocument(Document document) {
        this.documents.remove(document);
    }

    public void addFolder(Folder folder) {
        this.folders.add(folder);
    }

    public void addDocument(Document document) {
        this.documents.add(document);
    }

    public static Folder createFolder(String name, Folder parentFolder) {
        Folder folder = new Folder();
        folder.setName(name);
        folder.setParentFolder(parentFolder);
        return folder;
    }

    public static Folder createFolder(String name, Folder parentFolder, User user) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("folder's name can not be null or empty");
        }
        if (user == null) {
            throw new IllegalArgumentException("folder's user id can not be null");
        }
        Folder folder = new Folder();
        folder.setName(name);
        folder.setParentFolder(parentFolder);
        folder.setUser(user);
        return folder;
    }


}
