package docSharing.entity;

import lombok.Data;
import javax.persistence.*;

//@Entity
//@Data
public enum Permission {
    VIEWER,
    EDITOR,
    MODERATOR
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
}
