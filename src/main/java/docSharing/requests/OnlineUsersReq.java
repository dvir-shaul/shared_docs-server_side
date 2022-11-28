package docSharing.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class OnlineUsersReq {
    private Long userId;
    private Long documentId;
    private Method method;


}
