package docSharing.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class UserDocument {
    @EmbeddedId
    private UserDocumentPk id;

    @ManyToOne
    @MapsId("documentId")
    private Document document;
    @ManyToOne
    @MapsId("userId")
    private User user;

    @Enumerated
    @Column(name = "permission")
    private Permission permission;
}
