package docSharing.entity;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Log {
    private LocalDateTime creationDate = LocalDateTime.now();
    private LocalDateTime lastEditDate;
    private Integer offset;
    private String action;
    private String data;
    private Long userId;
    private Long documentId;
    private String token;

    public static Log copy(Log log){
        Log tempLog = new Log();
        tempLog.setUserId(log.getUserId());
        tempLog.setOffset(log.getOffset());
        tempLog.setCreationDate(log.getCreationDate());
        tempLog.setDocumentId(log.getDocumentId());
        tempLog.setData(log.getData());
        tempLog.setAction(log.getAction());
        tempLog.setToken(log.getToken());
        return tempLog;
    }
}
