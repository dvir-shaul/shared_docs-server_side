package docSharing.service;

public interface ServiceInterface {
    public Long create(Long id, String name, Long folderId);
    public int rename(Long id, String name);
    public int relocate(Long folderId, Long id);
    public void delete(Long id);
}
