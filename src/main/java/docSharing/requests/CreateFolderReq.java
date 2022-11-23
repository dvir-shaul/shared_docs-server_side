package docSharing.requests;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CreateFolderReq {
    private String name;
    private Long parentFolderId;

    public CreateFolderReq(String name, Long parentFolderId) {
        this.name = name;
        this.parentFolderId = parentFolderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentFolderId() {
        return parentFolderId;
    }

    public void setParentFolderId(Long parentFolderId) {
        this.parentFolderId = parentFolderId;
    }
}
