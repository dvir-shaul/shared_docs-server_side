package docSharing.entity;

import docSharing.utils.ExceptionMessage;

import javax.persistence.*;
import java.time.LocalDate;

@MappedSuperclass
public class GeneralItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDate creationDate;
    private Long userId;
    private Long parentFolderId;
    private String name;

    public GeneralItem(){
        this.creationDate = LocalDate.now();
    };

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        if(userId == null) throw new IllegalArgumentException(ExceptionMessage.NULL_INPUT.toString());
        this.userId = userId;
    }

    public Long getParentFolderId() {
        return parentFolderId;
    }

    public void setParentFolderId(Long parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(name == null) throw new IllegalArgumentException(ExceptionMessage.NULL_INPUT.toString());
        this.name = name;
    }

    @Override
    public String toString() {
        return "File{" +
                "id=" + id +
                ", creationDate=" + creationDate +
                ", userId=" + userId +
                ", parentFolderId=" + parentFolderId +
                ", name='" + name + '\'' +
                "}\n";
    }
}
