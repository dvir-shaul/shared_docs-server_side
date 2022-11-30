package docSharing.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateDocumentReq {
    private String name;
    private Long parentFolderId;
    private String content;

    @Override
    public String toString() {
        return "CreateDocumentReq{" +
                "name='" + name + '\'' +
                ", parentFolderId=" + parentFolderId +
                ", content='" + content + '\'' +
                '}';
    }
}
