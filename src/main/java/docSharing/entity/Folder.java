package docSharing.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity(name = "Folder")
@Table(name = "folder")
public class Folder extends GeneralItem {

    private Folder() {
        super();
    }

    public static Folder createFolder(String name, Long parentFolderId, Long userId) {
        Folder folder = new Folder();
        folder.setName(name);
        folder.setParentFolderId(parentFolderId);
        folder.setUserId(userId);
        return folder;
    }
}
