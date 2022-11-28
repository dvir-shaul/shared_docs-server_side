package docSharing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import docSharing.utils.ExceptionMessage;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;

@MappedSuperclass
public class GeneralItem {
    @Id
    @GeneratedValue
    @GenericGenerator(
            name = "sequence-generator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
            @org.hibernate.annotations.Parameter(name = "initial_value", value = "10")
    })
    private Long id;
    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDate creationDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "parent_folder_id")
    @JsonIgnore
    private Folder parentFolder;
    private String name;

    public Folder getParentFolder() {
        return parentFolder;
    }

    public void setParentFolder(Folder parentFolder) {
        this.parentFolder = parentFolder;
    }

    public GeneralItem() {
        this.creationDate = LocalDate.now();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) throw new IllegalArgumentException(ExceptionMessage.NULL_INPUT.toString());
        this.name = name;
    }

    @Override
    public String toString() {
        return "File{" +
                "id=" + id +
                ", creationDate=" + creationDate +
                ", user=" + user +
                ", parentFolder=" + parentFolder +
                ", name='" + name + '\'' +
                "}\n";
    }
}
