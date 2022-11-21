package docSharing.entity;

import javax.persistence.*;

@Entity(name = "Folder")
@Table(name = "folder")
public class Folder extends GeneralItem {

    private Folder() {
        super();
    }

    public static Folder createFolder(String name, Long parentFolderId, Long userId) {
        if(name==null||name.length()==0){
            throw new IllegalArgumentException("folder's name can not be null or empty");
        }
        if(userId==null){
            throw new IllegalArgumentException("folder's user id can not be null");
        }
        Folder folder = new Folder();
        folder.setName(name);
        folder.setParentFolderId(parentFolderId);
        folder.setUserId(userId);
        return folder;
    }

}
