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
    private Integer offset;
    private String action;
    private String data;
    private Long userId;
    private Long documentId;

}
