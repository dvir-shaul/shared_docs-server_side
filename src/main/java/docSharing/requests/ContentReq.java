package docSharing.requests;

import lombok.Data;

@Data
public class ContentReq {
    private String content;
    public ContentReq(){
        content="";
    }
}
