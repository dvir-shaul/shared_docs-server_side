package docSharing.entity;

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
        this.name = name;
    }

    @Override
    public String toString() {
        return "GeneralItem{" +
                "id=" + id +
                ", creationDate=" + creationDate +
                ", userId=" + userId +
                ", parentFolderId=" + parentFolderId +
                ", name='" + name + '\'' +
                '}';
    }
}
