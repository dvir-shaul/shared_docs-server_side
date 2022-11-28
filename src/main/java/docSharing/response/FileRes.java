package docSharing.response;

import docSharing.requests.Type;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class FileRes {
    private String name;
    private Long id;
    private Type type;
}
