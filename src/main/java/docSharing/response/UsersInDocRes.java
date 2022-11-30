package docSharing.response;

import docSharing.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UsersInDocRes {
    private Long id;
    private String name;
    private String email;
    private Permission permission;
}
