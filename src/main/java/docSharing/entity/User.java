package docSharing.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    private Boolean isActivated;
    @JsonManagedReference
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Document> documents = new HashSet<>();
    @JsonManagedReference
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Folder> folders = new HashSet<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Log> logs;

    public void addDocument(Document document) {
        this.documents.add(document);
    }

    public void addFolder(Folder folder) {
        this.folders.add(folder);
    }

    public void addLog(Log log) {
        this.logs.add(log);
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
    public static User createUserForLoginTest(String email, String password) {
        User user = new User();
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
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("name can not be null or empty");
        }
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.length() == 0) {
            throw new IllegalArgumentException("email can not be null or empty");
        }
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.length() == 0) {
            throw new IllegalArgumentException("password can not be null or empty");
        }
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
        int result = Math.toIntExact(id);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }

//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result
//                + ((importantField == null) ? 0 : importantField.hashCode());
//        return result;
//    }

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
