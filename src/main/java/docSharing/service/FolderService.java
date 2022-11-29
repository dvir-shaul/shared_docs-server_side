package docSharing.service;

import docSharing.entity.Document;
import docSharing.entity.GeneralItem;
import docSharing.entity.Folder;
import docSharing.entity.User;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.utils.ExceptionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class FolderService implements ServiceInterface {

    @Autowired
    FolderRepository folderRepository;
    @Autowired
    DocumentRepository documentRepository;

    /**
     * @param id - id of folder in database
     * @return - Folder entity from database.
     * @throws AccountNotFoundException - no such folder in database.
     */
    public Folder findById(Long id) throws AccountNotFoundException {
        if(! folderRepository.findById(id).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_FOLDER_IN_DATABASE.toString());
        return folderRepository.findById(id).get();
    }

    /**
     * @param parentFolderId - parent folder to search and bring all items from.
     * @param userId - current user that ask for the list of folders
     * @return - list of inner folders in parent folder.
     */
    public List<Folder> get(Long parentFolderId, Long userId) {
        return folderRepository.findAllByParentFolderIdAndUserId(parentFolderId, userId);
    }

    /**
     * function get an item of kind folder and uses the logics to create and save a new folder to database.
     * @param generalItem - create item
     * @return id of the item that was saved to database.
     */
    public Long create(GeneralItem generalItem) {
        if (generalItem.getParentFolder() != null) {
            Optional<Folder> folder = folderRepository.findById(generalItem.getParentFolder().getId());
            if (!folder.isPresent())
                throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString() + generalItem.getParentFolder().getId());
        }
        Folder savedFolder = folderRepository.save((Folder) generalItem);
        if (savedFolder.getParentFolder() != null) {
            savedFolder.getParentFolder().addFolder(savedFolder);
        }
        savedFolder.getUser().addFolder(savedFolder);
        return savedFolder.getId();
    }

    /**
     * rename function gets an id of folder and new name to change the folder's name.
     * @param id   - document id.
     * @param name - new name of the document.
     * @return rows affected in mysql.
     */
    public int rename(Long id, String name) {
        if (folderRepository.findById(id).isPresent()) {
            return folderRepository.updateName(name, id);
        }
        throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
    }

    /**
     * relocate is to change the document's location.
     * @param newParentFolder - the folder that folder is located.
     * @param id              - document id.
     * @return rows affected in mysql.
     */
    public int relocate(Folder newParentFolder, Long id) {
        if (newParentFolder != null && !folderRepository.findById(newParentFolder.getId()).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
        }
        if (!folderRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
        }
        Folder folder = folderRepository.findById(id).get();
        if (newParentIsChild(newParentFolder, folder)) {
            throw new IllegalArgumentException(ExceptionMessage.CIRCULAR_FOLDERS.toString());
        }

        Folder oldParentFolder = folder.getParentFolder();
        folder.setParentFolder(newParentFolder);
        if (oldParentFolder != null) {
            oldParentFolder.removeFolder(folder);
        }
        if (newParentFolder != null) {
            newParentFolder.addFolder(folder);
        }
        return documentRepository.updateParentFolderId(newParentFolder, id);
    }

    /**
     * delete folder by getting the document id, and deleting all it's content by recursively
     * going through the folder files and folders in it.
     *
     * @param id - gets folder id to start delete the content.
     */
    public void delete(Long id) {
        folderRepository.deleteById(id);
    }

    private boolean newParentIsChild(Folder targetFolder, Folder destinationFolder) {
        if (destinationFolder.getFolders().isEmpty()) {
            return false;
        }
        if (destinationFolder.getFolders().contains(targetFolder)) {
            return true;
        }
        for (Folder folder :
                destinationFolder.getFolders()) {
            if (newParentIsChild(targetFolder, folder)) {
                return true;
            }
        }
        return false;
    }
}
