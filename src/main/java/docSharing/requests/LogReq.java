package docSharing.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LogReq {
    private Long userId;
    private Long documentId;
    private int offset;
    private String data;
    private String action;
}
