package docSharing.requests;

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
    private String action;
}
