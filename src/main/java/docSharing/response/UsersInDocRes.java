package docSharing.response;

import docSharing.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Comparator;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UsersInDocRes{
    private Long id;
    private String name;
    private String email;
    private Permission permission;
    private UserStatus status;



    public int compareTo(UsersInDocRes o1) {
        return this.getStatus().compareTo(o1.getStatus());
    }
}
