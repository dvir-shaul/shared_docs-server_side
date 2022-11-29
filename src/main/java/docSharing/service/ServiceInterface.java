package docSharing.service;

import docSharing.entity.Folder;
import docSharing.entity.GeneralItem;

public interface ServiceInterface {
    Long create(GeneralItem generalItem);
    int rename(Long id, String name);
    int relocate(Folder folderId, Long id);
    void delete(Long id);
}
