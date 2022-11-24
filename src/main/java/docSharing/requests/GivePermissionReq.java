package docSharing.requests;

import docSharing.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GivePermissionReq {
    private Long documentId;
    private Long userId;
    private Permission permission;
}
