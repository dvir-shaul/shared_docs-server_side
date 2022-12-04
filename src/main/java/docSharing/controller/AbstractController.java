package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.GeneralItem;
import docSharing.entity.Folder;
import docSharing.entity.Permission;
import docSharing.requests.Type;
import docSharing.response.FileRes;
import docSharing.service.*;
import docSharing.utils.Regex;
import docSharing.utils.Validations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.security.auth.login.AccountNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Component
public class AbstractController {

    private static Logger logger = LogManager.getLogger(AbstractController.class.getName());

    @Autowired
    DocumentService documentService;
    @Autowired
    FolderService folderService;
    @Autowired
    UserService userService;
    /**
     * getAll function called from the client when we enter a new folder, and it should send the client a list with all
     * the folders & documents to present the client.
     *
     * @param parentFolderId - folder id.
     * @param userId         - the user id.
     * @return response entity with a List<FileRes> with all the folders & documents to send.
     */
    public ResponseEntity<List<FileRes>> getAll(Long parentFolderId, Long userId) {
        logger.info("in AbstractController -> getAll");

        List<Folder> folders;
        List<Document> documents;
        if (parentFolderId != null) {
            try {
                folders = folderService.get(parentFolderId, userId);
                documents = documentService.get(parentFolderId, userId);
            } catch (AccountNotFoundException e) {
                logger.error("in AbstractController -> getAll -> " + e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            try {
                folders = folderService.getAllWhereParentFolderIsNull(userId);
                documents = documentService.getAllWhereParentFolderIsNull(userId);

            } catch (AccountNotFoundException e) {
                logger.error("in AbstractController -> getAll -> " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return ResponseEntity.ok().body(convertToFileRes(folders, documents));
    }
    /**
     * convertToFileRes is an inner function of getAll, that gets a list of folders & documents,
     * and return a list of FileRes entity which has the name,id and the type of a given file
     * for the convenient of the client side which need
     * to show all the files to user.
     *
     * @param folders   - List<Folder> to present.
     * @param documents - List<Document> to present
     * @return - List<FileRes>
     */
    private List<FileRes> convertToFileRes(List<Folder> folders, List<Document> documents) {
        logger.info("in AbstractController -> convertToFileRes");
        List<FileRes> fileResList = new ArrayList<>();
        for (Folder folder :
                folders) {

            fileResList.add(new FileRes(folder.getName(), folder.getId(), Type.FOLDER, Permission.ADMIN, folder.getUser().getEmail()));
        }
        for (Document document :
                documents) {
            fileResList.add(new FileRes(document.getName(), document.getId(), Type.DOCUMENT, Permission.ADMIN, document.getUser().getEmail()));
        }
        return fileResList;
    }

    /**
     * create is a request from the client to create a new file in a specific folder location.
     *
     * @param item - item of kind folder or document.
     * @param c    - the class of the item, need to know to what service sends the request.
     * @return - ResponseEntity.
     */
    public ResponseEntity<String> create(GeneralItem item, Class c) {
        // make sure we got all the data from the client
        logger.info("in AbstractController -> create");
        try {
            Validations.validate(Regex.FILE_NAME.getRegex(), item.getName());
//            Validations.validate(Regex.ID.getRegex(), item.getParentFolderId().toString());
        } catch (NullPointerException e) {
            logger.error("in AbstractController -> create -> " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("in AbstractController -> create -> " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return ResponseEntity.ok().body(convertFromClassToService(c).create(item).toString());
    }
    /**
     * rename a file, called from the fileController with a request to change name.
     *
     * @param id   - of a file.
     * @param name - new name.
     * @param c    - the class of the item, need to know to what service sends the request.
     * @return - ResponseEntity.
     */
    public ResponseEntity<Object> rename(Long id, String name, Class c) {
        logger.info("in AbstractController -> rename");
        if (name == null){
            logger.error("in AbstractController -> rename -> name is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You must include all and exact parameters for such an action: name");
        }

        return ResponseEntity.ok().body(String.valueOf(convertFromClassToService(c).rename(id, name)));
    }
    /**
     * delete a file, called from the fileController with a request to delete.
     *
     * @param id - of a file to delete it.
     * @param c  - the class of the item, need to know to what service sends the request.
     * @return - ResponseEntity.
     */
    public ResponseEntity<String> delete(Long id, Class c) {
        logger.info("in AbstractController -> delete");
        if (id == null) {
            logger.error("in AbstractController -> delete -> id is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("I'm sorry. In order for me to delete a document, you need to be more specific about its id... So what is its id?");
        }
        convertFromClassToService(c).delete(id);
        return ResponseEntity.ok().body("An item answering to the id:" + id + " has been successfully erased from the database!");
    }
    /**
     * relocate a file, called from the fileController with a request to relocate.
     *
     * @param newParentId - the new folder that the new file will insert into.
     * @param id          - of a file
     * @param c           - the class of the item, need to know to what service sends the request.
     * @return - ResponseEntity
     */
    public ResponseEntity<Object> relocate(Long newParentId, Long id, Class c) {
        logger.info("in AbstractController -> relocate");
        Folder parentFolder = null;
        try {
            if (newParentId != null) {
                parentFolder = folderService.findById(newParentId);
            }
            if (id == null) {
                logger.error("in AbstractController -> relocate -> id is null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
            }
            return ResponseEntity.ok().body(convertFromClassToService(c).relocate(parentFolder, id));
        } catch (AccountNotFoundException e) {
            logger.error("in AbstractController -> relocate -> " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * This function gets an item as a parameter and extracts its class in order to return the correct service.
     *
     * @param c - class of folder/document
     * @return the service we need to use according to what file it is.
     */
    private ServiceInterface convertFromClassToService(Class c) {
        logger.info("in AbstractController -> convertFromClassToService");
        if (c.equals(Document.class)) return documentService;
        if (c.equals(Folder.class)) return folderService;
        return null;
    }
}