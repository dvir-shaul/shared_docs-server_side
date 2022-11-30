package docSharing.requests;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class OnlineUsersReq {
    private Long userId;
    private Method method;
}
