package docSharing.service;

import docSharing.entity.Document;
import docSharing.entity.GeneralItem;
import docSharing.entity.Folder;
import docSharing.entity.User;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserDocumentRepository;
import docSharing.repository.UserRepository;
import docSharing.utils.ExceptionMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
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

    /**
     * @param id - id of folder in database
     * @return - Folder entity from database.
     * @throws AccountNotFoundException - no such folder in database.
     */
    public Folder findById(Long id) throws AccountNotFoundException {
        logger.info("in FolderService -> findById");

        if (!folderRepository.findById(id).isPresent()){
            logger.error("in FolderService -> findById --> "+ExceptionMessage.NO_FOLDER_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_FOLDER_IN_DATABASE.toString());

        }
        return folderRepository.findById(id).get();
    }

    /**
     * @param parentFolderId - parent folder to search and bring all items from.
     * @param userId         - current user that ask for the list of folders
     * @return - list of inner folders in parent folder.
     */

    public List<Folder> get(Long parentFolderId, Long userId) throws AccountNotFoundException {
        logger.info("in FolderService -> get");

        if (!folderRepository.findById(parentFolderId).isPresent()){
            logger.error("in FolderService -> get --> "+ExceptionMessage.NO_FOLDER_IN_DATABASE);

            throw new AccountNotFoundException(ExceptionMessage.NO_FOLDER_IN_DATABASE.toString());

        }
        if (!userRepository.findById(userId).isPresent()){
            logger.error("in FolderService -> get --> "+ExceptionMessage.NO_USER_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());

        }
        Folder parentFolder = folderRepository.findById(parentFolderId).get();
        User user = userRepository.findById(userId).get();
        return folderRepository.findAllByParentFolderIdAndUserId(parentFolder, user);
    }

    public List<Folder> getAllWhereParentFolderIsNull(Long userId) throws AccountNotFoundException {
        logger.info("in FolderService -> getAllWhereParentFolderIsNull");

        if (!userRepository.findById(userId).isPresent()){
            logger.error("in FolderService -> getAllWhereParentFolderIsNull --> "+ExceptionMessage.NO_USER_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        }
        User user = userRepository.findById(userId).get();
        return folderRepository.findAllByParentFolderIsNull(user);
    }

    /**
     * function get an item of kind folder and uses the logics to create and save a new folder to database.
     *
     * @param generalItem - create item
     * @return id of the item that was saved to database.
     */
    public Long create(GeneralItem generalItem) {
        logger.info("in FolderService -> create");

        if (generalItem.getParentFolder() != null) {
            Optional<Folder> folder = folderRepository.findById(generalItem.getParentFolder().getId());
            if (!folder.isPresent()){
                logger.error("in FolderService -> create --> "+ExceptionMessage.FOLDER_DOES_NOT_EXISTS);
                throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString() + generalItem.getParentFolder().getId());

            }
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
     *
     * @param id   - document id.
     * @param name - new name of the document.
     * @return rows affected in mysql.
     */
    public int rename(Long id, String name) {
        logger.info("in FolderService -> rename");
        if (folderRepository.findById(id).isPresent()) {
            return folderRepository.updateName(name, id);
        }
        logger.error("in FolderService -> rename --> "+ExceptionMessage.FOLDER_DOES_NOT_EXISTS);
        throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
    }

    /**
     * relocate is to change the document's location.
     *
     * @param newParentFolder - the folder that folder is located.
     * @param id              - document id.
     * @return rows affected in mysql.
     */
    public int relocate(Folder newParentFolder, Long id) {
        logger.info("in FolderService -> relocate");
        if (newParentFolder != null && !folderRepository.findById(newParentFolder.getId()).isPresent()) {
            logger.error("in FolderService -> relocate --> "+ExceptionMessage.FOLDER_DOES_NOT_EXISTS);
            throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
        }
        if (!folderRepository.findById(id).isPresent()) {
            logger.error("in FolderService -> relocate --> "+ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS);
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
        }
        Folder folder = folderRepository.findById(id).get();
        if (newParentIsChild(newParentFolder, folder)) {
            logger.error("in FolderService -> relocate --> "+ExceptionMessage.CIRCULAR_FOLDERS);
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
        return folderRepository.updateParentFolderId(newParentFolder, id);
    }


    private boolean newParentIsChild(Folder targetFolder, Folder destinationFolder) {
        logger.info("in FolderService -> newParentIsChild");
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

    public void delete(Long folderId) {
        logger.info("in FolderService -> delete");
        Folder folder = folderRepository.findById(folderId).get();
        folder.getDocuments().forEach(document -> {
            userDocumentRepository.deleteDocument(document);
            documentRepository.delete(document);
        });
        folder.getFolders().forEach(f -> {
            delete(f.getId());
        });
        folderRepository.delete(folder);
    }

    public void createRootFolders(User user) {
        logger.info("in FolderService -> createRootFolders");
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
