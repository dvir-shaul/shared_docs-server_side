package docSharing.requests;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CreateDocument {
    private String name;
    private Long parentFolderId;

    public CreateDocument(String name, Long parentFolderId) {
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
