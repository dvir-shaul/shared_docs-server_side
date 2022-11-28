package docSharing.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    private Boolean isActivated;
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Document> documents=new HashSet<>();
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Folder> folders=new HashSet<>();

    public void addDocument(Document document){
        this.documents.add(document);
    }
    public void addFolder(Folder folder){
        this.folders.add(folder);
    }
    public Set<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(Set<Document> documents) {
        this.documents = documents;
    }

    public Set<Folder> getFolders() {
        return folders;
    }

    public void setFolders(Set<Folder> folders) {
        this.folders = folders;
    }

    public User() {
        this.isActivated = false;
    }

    public static User createUser(String email, String password, String name) {
        if (email == null || email.length() == 0) {
            throw new IllegalArgumentException("email can not be null or empty");
        }
        if (password == null || password.length() == 0) {
            throw new IllegalArgumentException("password can not be null or empty");
        }
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("name can not be null or empty");
        }
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        user.setEmail(email);
        return user;
    }

    public Boolean getActivated() {
        return isActivated;
    }

    public void setActivated(Boolean activated) {
        isActivated = activated;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (id != user.id) return false;
        if (!Objects.equals(name, user.name)) return false;
        if (!Objects.equals(email, user.email)) return false;
        return Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

//    @Override
//    public String toString() {
//        return "User{" +
//                "id=" + id +
//                ", name='" + name + '\'' +
//                ", email='" + email + '\'' +
//                ", password='" + password + '\'' +
//                ", isActivated=" + isActivated +
//                ", documents=" + documents +
//                ", folders=" + folders +
//                '}';
//    }
}
