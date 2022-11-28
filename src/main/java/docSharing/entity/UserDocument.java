package docSharing.entity;

import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import javax.persistence.Entity;

@Entity
@Data
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UserDocument {
    @EmbeddedId
    private UserDocumentPk id = new UserDocumentPk();

    @ManyToOne
    @MapsId("documentId")
    @Cascade(CascadeType.DELETE)
    private Document document;

    @ManyToOne
    @MapsId("userId")
    @Cascade(CascadeType.DELETE)
    private User user;

    @Enumerated
    @Column(name = "permission")
    @Cascade(CascadeType.DELETE)
    private Permission permission;
}
