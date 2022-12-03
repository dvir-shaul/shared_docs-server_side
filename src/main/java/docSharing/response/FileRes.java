package docSharing.response;

import docSharing.entity.Permission;
import docSharing.requests.Type;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class FileRes {
    // CONSULT: FileRes == DocumentRes ?
    private String name;
    private Long id;
    private Type type;
    private Permission permission;
    private String adminEmail;
}
