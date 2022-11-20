package docSharing.service;

import docSharing.entity.GeneralItem;

public interface ServiceInterface {
    public Long create(GeneralItem generalItem);
    public int rename(Long id, String name);
    public int relocate(Long folderId, Long id);
    public void delete(Long id);
}
