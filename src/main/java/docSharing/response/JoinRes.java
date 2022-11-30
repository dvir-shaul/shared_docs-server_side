package docSharing.response;

import docSharing.entity.Permission;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class JoinRes {
    private String name;
    private Long userId;
    private Permission permission;
}
