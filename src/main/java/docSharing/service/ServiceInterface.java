package docSharing.service;

import docSharing.entity.Folder;
import docSharing.entity.User;
import docSharing.response.FileRes;

import java.io.FileNotFoundException;
import java.util.List;

public interface ServiceInterface {
    Long create(Folder parentFolder, User user, String name, String content) throws FileNotFoundException;
    int rename(long id, String name) throws FileNotFoundException;
    int relocate(Folder folderId, long id) throws FileNotFoundException;
    int delete(long id) throws FileNotFoundException;
    Boolean doesExist(long id);
    List<FileRes> getPath(long itemId) throws FileNotFoundException;
}
