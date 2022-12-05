package docSharing.service;

import docSharing.entity.Folder;
import docSharing.entity.GeneralItem;
import docSharing.response.FileRes;

import java.io.FileNotFoundException;
import java.util.List;

public interface ServiceInterface {
    Long create(GeneralItem generalItem);
    int rename(Long id, String name);
    int relocate(Folder folderId, Long id) throws FileNotFoundException;
    void delete(Long id) throws FileNotFoundException;
    Boolean doesExist(Long id);
    List<FileRes> getPath(GeneralItem generalItem);
//    GeneralItem get(Long id);
}
