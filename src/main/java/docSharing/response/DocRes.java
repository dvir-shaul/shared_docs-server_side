package docSharing.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DocRes {
    private String name;
    private Long adminId;
    private boolean isPrivate;
    private LocalDate creationDate;
    private Long parentFolderId;
    private Long id;
}
