package docSharing.service;

import docSharing.entity.Folder;
import docSharing.entity.GeneralItem;

public interface ServiceInterface {
    public Long create(GeneralItem generalItem);
    public int rename(Long id, String name);
    public int relocate(Folder folderId, Long id);
    public void delete(Long id);
}
