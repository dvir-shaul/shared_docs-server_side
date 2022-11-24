package docSharing.entity;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class UserDocumentPk implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long userId;
    private Long documentId;
}
