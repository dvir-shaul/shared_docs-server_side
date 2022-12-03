package docSharing.service;

import docSharing.entity.Folder;
import docSharing.entity.GeneralItem;

import java.io.FileNotFoundException;

public interface ServiceInterface {
    Long create(GeneralItem generalItem);
    int rename(Long id, String name);
    int relocate(Folder folderId, Long id) throws FileNotFoundException;
    void delete(Long id) throws FileNotFoundException;
    Boolean doesExist(Long id);
//    GeneralItem get(Long id);
}
