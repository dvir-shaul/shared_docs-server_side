package docSharing.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class OnlineUsersReq {
    private Long userId;
    private Long documentId;
    private Method method;
    private String token;

}
