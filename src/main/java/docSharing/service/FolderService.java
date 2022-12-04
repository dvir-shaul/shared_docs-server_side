package docSharing.service;

import docSharing.entity.*;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserDocumentRepository;
import docSharing.repository.UserRepository;
import docSharing.requests.Type;
import docSharing.response.FileRes;
import docSharing.utils.ExceptionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FolderService implements ServiceInterface {

    @Autowired
    FolderRepository folderRepository;
    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserDocumentRepository userDocumentRepository;

    /**
     * @param id - id of folder in database
     * @return - Folder entity from database.
     * @throws AccountNotFoundException - no such folder in database.
     */
    public Folder findById(Long id) throws FileNotFoundException {
        Optional<Folder> folder = folderRepository.findById(id);
        if (!folder.isPresent())
            throw new FileNotFoundException(ExceptionMessage.NO_FOLDER_IN_DATABASE.toString());

        return folder.get();
    }

    /**
     * @param parentFolderId - parent folder to search and bring all items from.
     * @param userId         - current user that ask for the list of folders
     * @return - list of inner folders in parent folder.
     */

    public List<Folder> get(Long parentFolderId, Long userId) throws AccountNotFoundException {
        if (!folderRepository.findById(parentFolderId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_FOLDER_IN_DATABASE.toString());
        if (!userRepository.findById(userId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        Folder parentFolder = folderRepository.findById(parentFolderId).get();
        User user = userRepository.findById(userId).get();
        return folderRepository.findAllByParentFolderIdAndUserId(parentFolder, user);
    }

    public List<Folder> getAllWhereParentFolderIsNull(Long userId) throws AccountNotFoundException {
        if (!userRepository.findById(userId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        User user = userRepository.findById(userId).get();
        return folderRepository.findAllByParentFolderIsNull(user);
    }

    /**
     * function get an item of kind folder and uses the logics to create and save a new folder to database.
     *
     * @param parentFolder - parent folder of the folder
     * @param user - the owner of the folder
     * @param name - name of folder
     * @param content - not in use here
     * @return id of the item that was saved to database.
     */
    public Long create(Folder parentFolder, User user, String name, String content) {
        if (parentFolder == null) {
                throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString() + parentFolder.getId());
        }
        Folder folder = Folder.createFolder(name, parentFolder, user);
        Folder savedFolder = folderRepository.save(folder);
        if (savedFolder.getParentFolder() != null) {
            savedFolder.getParentFolder().addFolder(savedFolder);
        }
        savedFolder.getUser().addFolder(savedFolder);
        return savedFolder.getId();
    }
public List<FileRes> getPath(GeneralItem generalItem){
    List<FileRes> path = new ArrayList<>();
    Folder parentFolder = generalItem.getParentFolder();
        path.add(0, new FileRes(generalItem.getName(), generalItem.getId(), Type.FOLDER, Permission.ADMIN, generalItem.getUser().getEmail()));
    while (parentFolder != null) {
        path.add(0, new FileRes(parentFolder.getName(), parentFolder.getId(), Type.FOLDER, Permission.ADMIN, generalItem.getUser().getEmail()));
        parentFolder = parentFolder.getParentFolder();
    }
    return path;
}
    /**
     * rename function gets an id of folder and new name to change the folder's name.
     *
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
     *
     * @param newParentFolder - the folder that folder is located.
     * @param id              - document id.
     * @return rows affected in mysql.
     */
    public int relocate(Folder newParentFolder, Long id) throws FileNotFoundException {
        if (newParentFolder != null && !folderRepository.findById(newParentFolder.getId()).isPresent())
            throw new FileNotFoundException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());

        Optional<Folder> folder = folderRepository.findById(id);
        if (!folder.isPresent())
            throw new FileNotFoundException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());

        if (newParentIsChild(newParentFolder, folder.get()))
            throw new IllegalArgumentException(ExceptionMessage.CIRCULAR_FOLDERS.toString());

        Folder oldParentFolder = folder.get().getParentFolder();

        folder.get().setParentFolder(newParentFolder);
        if (oldParentFolder != null)
            oldParentFolder.removeFolder(folder.get());

        if (newParentFolder != null)
            newParentFolder.addFolder(folder.get());

        return folderRepository.updateParentFolderId(newParentFolder, id);
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

    public void delete(Long folderId) throws FileNotFoundException {
        Optional<Folder> folder = folderRepository.findById(folderId);
        if (!folder.isPresent())
            throw new FileNotFoundException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());

        folder.get().getDocuments().forEach(document -> {
            userDocumentRepository.deleteDocument(document);
            documentRepository.delete(document);
        });

        for (Folder f : folder.get().getFolders()) {
            delete(f.getId());
        }

        folderRepository.delete(folder.get());
    }

    public Boolean doesExist(Long id){
        return folderRepository.findById(id).isPresent();
    }

    public void createRootFolders(User user) {
        Folder general = Folder.createFolder("General", null, user);
        folderRepository.save(general);
        Folder personal = Folder.createFolder("Personal", null, user);
        folderRepository.save(personal);
        Folder programming = Folder.createFolder("Programming", null, user);
        folderRepository.save(programming);
        Folder design = Folder.createFolder("Design", null, user);
        folderRepository.save(design);
        Folder business = Folder.createFolder("Business", null, user);
        folderRepository.save(business);
    }
}
