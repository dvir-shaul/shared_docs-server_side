package docSharing.service;

import docSharing.entity.*;
import docSharing.repository.*;
import docSharing.requests.Type;
import docSharing.response.FileRes;
import docSharing.utils.ExceptionMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FolderService implements ServiceInterface {
    private static Logger logger = LogManager.getLogger(FolderService.class.getName());


    @Autowired
    FolderRepository folderRepository;
    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserDocumentRepository userDocumentRepository;
    @Autowired
    DocumentService documentService;

    /**
     * @param id - id of folder in database
     * @return - Folder entity from database.
     * @throws FileNotFoundException - no such folder in database.
     */
    public Folder findById(Long id) throws FileNotFoundException {
        logger.info("in FolderService -> findById");
        Optional<Folder> folder = folderRepository.findById(id);
        if (!folder.isPresent()) {
            logger.error("in FolderService -> findById --> " + ExceptionMessage.NO_FOLDER_IN_DATABASE);
            throw new FileNotFoundException(ExceptionMessage.NO_FOLDER_IN_DATABASE.toString());
        }
        return folder.get();
    }

    /**
     * this function gets called when we want to show to te client all the folders that in a specific folder.
     *
     * @param parentFolderId - parent folder to search and bring all items from.
     * @param userId         - current user that ask for the list of folders
     * @return - list of inner folders in parent folder.
     */
    public List<Folder> get(long parentFolderId, long userId) throws AccountNotFoundException, FileNotFoundException {
        logger.info("in FolderService -> get");
        Optional<Folder> folder = folderRepository.findById(parentFolderId);
        if (!folder.isPresent()) {
            logger.error("in FolderService -> get --> " + ExceptionMessage.NO_FOLDER_IN_DATABASE);
            throw new FileNotFoundException(ExceptionMessage.NO_FOLDER_IN_DATABASE.toString());
        }

        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            logger.error("in FolderService -> get --> " + ExceptionMessage.NO_USER_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        }

        return folderRepository.findAllByParentFolderIdAndUserId(folder.get(), user.get());
    }

    /**
     * get called by FacadeFileController in getAll function, that's need to return all the data files, when
     * the parent folder is null.
     * function gets called when parent folder is null, called for basic built in folders.
     *
     * @param userId - user's relation folders
     * @return - list of folders
     */
    public List<Folder> getAllWhereParentFolderIsNull(long userId) throws AccountNotFoundException {
        logger.info("in FolderService -> getAllWhereParentFolderIsNull, userId:" + userId);
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            logger.error("in FolderService -> getAllWhereParentFolderIsNull --> " + ExceptionMessage.NO_USER_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        }
        return folderRepository.findAllByParentFolderIsNull(user.get());
    }

    /**
     * create is creating a new folder in the database.
     * first check if we have a folder to create the folder into.
     * then create the folder and save the folder the folderRepository in the database,
     * the user is needed to be assigned to the new document.
     *
     * @param parentFolder - parent folder of the folder
     * @param user         - the owner of the folder
     * @param name         - name of folder
     * @param content      - not in use here
     * @return id of the item that was saved to database.
     */
    public Long create(Folder parentFolder, User user, String name, String content) throws FileNotFoundException {
        logger.info("in FolderService -> create, item :" + name);

        if (parentFolder != null) {
            Optional<Folder> folder = folderRepository.findById(parentFolder.getId());
            if (!folder.isPresent()) {
                throw new FileNotFoundException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
            }
        }

        Folder folder = Folder.createFolder(name, parentFolder, user);
        Folder savedFolder = folderRepository.save(folder);
        if (savedFolder.getParentFolder() != null) {
            savedFolder.getParentFolder().addFolder(savedFolder);
        }
        savedFolder.getUser().addFolder(savedFolder);
        return savedFolder.getId();
    }

    /**
     * getPath called from FacadeFileController when we enter a folder inside the client side,
     * and want to present the client the new path he has done so far.
     *
     * @param folderId - folder id in the database.
     * @return - List of FileRes
     */
    public List<FileRes> getPath(long folderId) throws FileNotFoundException {
        logger.info("in FolderService -> getPath, item id is:" + folderId);
        Folder folder = findById(folderId);
        List<FileRes> path = new ArrayList<>();
        Folder parentFolder = folder.getParentFolder();
        path.add(0, new FileRes(folder.getName(), folder.getId(), Type.FOLDER, Permission.ADMIN, folder.getUser().getEmail()));
        while (parentFolder != null) {
            path.add(0, new FileRes(parentFolder.getName(), parentFolder.getId(), Type.FOLDER, Permission.ADMIN, folder.getUser().getEmail()));
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
    public int rename(long id, String name) throws FileNotFoundException {
        logger.info("in FolderService -> rename, id:" + id + " name:" + name);
        if (!folderRepository.findById(id).isPresent()) {
            logger.error("in FolderService -> rename --> " + ExceptionMessage.FOLDER_DOES_NOT_EXISTS);
            throw new FileNotFoundException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
        }
        return folderRepository.updateName(name, id);
    }

    /**
     * relocate is to change the document's location.
     * checks if the newParentFolder is not null and new folder exist
     * checks if we have the folder id in the database.
     * checks if there are CIRCULAR FOLDERS.
     * then update the folders for the action to take.
     *
     * @param newParentFolder - the folder that folder is located.
     * @param id              - document id.
     * @return rows affected in mysql.
     */
    public int relocate(Folder newParentFolder, long id) throws FileNotFoundException {
        logger.info("in FolderService -> relocate");
        if (newParentFolder != null && !folderRepository.findById(newParentFolder.getId()).isPresent()) {
            logger.error("in FolderService -> relocate --> " + ExceptionMessage.FOLDER_DOES_NOT_EXISTS);
            throw new FileNotFoundException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
        }

        Optional<Folder> folder = folderRepository.findById(id);
        if (!folder.isPresent()) {
            logger.error("in FolderService -> relocate --> " + ExceptionMessage.FOLDER_DOES_NOT_EXISTS);
            throw new FileNotFoundException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
        }

        if (newParentIsChild(newParentFolder, folder.get())) {
            logger.error("in FolderService -> relocate --> " + ExceptionMessage.CIRCULAR_FOLDERS);
            throw new IllegalArgumentException(ExceptionMessage.CIRCULAR_FOLDERS.toString());
        }

        Folder oldParentFolder = folder.get().getParentFolder();

        folder.get().setParentFolder(newParentFolder);
        if (oldParentFolder != null)
            oldParentFolder.removeFolder(folder.get());

        if (newParentFolder != null)
            newParentFolder.addFolder(folder.get());

        return folderRepository.updateParentFolderId(newParentFolder, id);
    }

    /**
     * newParentIsChild is private inner function that are boolean method, used in relocate.
     * goal deny a folder relocation to inner folder.
     *
     * @param targetFolder      - the new folder location.
     * @param destinationFolder - the folder we change the location.
     * @return - true if a folder is inner folder.
     */
    private boolean newParentIsChild(Folder targetFolder, Folder destinationFolder) {
        logger.info("in FolderService -> newParentIsChild");
        if (destinationFolder.getFolders().isEmpty()) {
            return false;
        }
        if (destinationFolder.getFolders().contains(targetFolder)) {
            return true;
        }
        for (Folder folder : destinationFolder.getFolders()) {
            if (newParentIsChild(targetFolder, folder)) {
                return true;
            }
        }
        return false;
    }

    /**
     * delete function called when user want to delete specific folder and all its content
     *
     * @param folderId - folder to delete.
     * @return int - amount of lines affected
     */
    public int delete(long folderId) throws FileNotFoundException {
        logger.info("in FolderService -> delete");
        Optional<Folder> folder = folderRepository.findById(folderId);
        if (!folder.isPresent()) {
            logger.error("in FolderService -> relocate --> folderId:" + folderId + "->" + ExceptionMessage.FOLDER_DOES_NOT_EXISTS);
            throw new FileNotFoundException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
        }
        for (Document document : folder.get().getDocuments()) {
            documentService.delete(document.getId());
        }
        for (Folder f : folder.get().getFolders()) {
            delete(f.getId());
        }
        folderRepository.delete(folder.get());
        return 1;
    }

    /**
     * checks if folder id is existed in database.
     *
     * @param id - of document
     * @return - true if documentRepository.findById(id).isPresent()
     */
    public Boolean doesExist(long id) {
        return folderRepository.findById(id).isPresent();
    }

    /**
     * createRootFolders is a function that called when a user is signed in,
     * idea is that a user will have the basics folders and from those he will navigate through his own files.
     *
     * @param user - given user.
     */
    public void createRootFolders(User user) throws AccountNotFoundException {
        logger.info("in FolderService -> createRootFolders");

        if(!userRepository.findById(user.getId()).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.ACCOUNT_DOES_NOT_EXISTS.toString());

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
