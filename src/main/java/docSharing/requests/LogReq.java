package docSharing.requests;

import docSharing.utils.logAction;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class LogReq {
    private Long userId;
    private Long documentId;
    private int offset;
    private String data;
    private logAction action;
}
