package docSharing.service;

import docSharing.entity.File;

public interface ServiceInterface {
    public Long create(File file);
    public int rename(Long id, String name);
    public int relocate(Long folderId, Long id);
    public void delete(Long id);
}
