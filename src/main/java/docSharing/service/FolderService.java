package docSharing.service;

import docSharing.entity.Folder;
import docSharing.repository.FolderRepository;
import docSharing.utils.ExceptionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FolderService implements ServiceInterface {

    @Autowired
    FolderRepository folderRepository;

    public Long create(Long userId, String name, Long parentFolderId) {
        if (parentFolderId != null) {
            System.out.println(parentFolderId + " is not null?");
            Optional<Folder> doc = folderRepository.findById(parentFolderId);
            if (!doc.isPresent())
                throw new IllegalArgumentException(ExceptionMessage.FOLDER_EXISTS.toString() + parentFolderId);
        }

        return folderRepository.save(Folder.createFolder(name, parentFolderId, userId)).getId();
    }

    public int rename(Long id, String name) {
        // TODO: make sure this folder exists in the db
        System.out.println("I am renaming this folder! " + id);
        return folderRepository.updateName(name, id);
    }

    public int relocate(Long parentFolderId, Long id) {
        // TODO: make sure both folders exist in the db
        return folderRepository.updateParentFolderId(parentFolderId, id);
    }

    public void delete(Long id) {
        // FIXME: delete the folder and every doc inside it and its sub folders!
        // TODO: make sure this folder exists in the db
        folderRepository.deleteById(id);
    }
}
