package docSharing.controller;

import docSharing.entity.*;
import docSharing.requests.*;
import docSharing.response.FileRes;
import docSharing.response.ExportDoc;
import docSharing.response.JoinRes;
import docSharing.service.AuthService;
import docSharing.service.DocumentService;
import docSharing.service.FolderService;
import docSharing.service.UserService;
import docSharing.utils.ExceptionMessage;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.*;

@Controller
@RequestMapping(value = "/file")
@CrossOrigin
@AllArgsConstructor
@NoArgsConstructor
class FileController {
    private static Logger logger = LogManager.getLogger(FileController.class.getName());

    @Autowired
    AbstractController ac;
    @Autowired
    FolderService folderService;
    @Autowired
    DocumentService documentService;
    @Autowired
    UserService userService;

    /**
     * getAll function called from the client when we enter a new folder, and it should send the client a list with all
     * the folders & documents to present the client.
     * @param parentFolderId - folder id.
     * @param userId - the user id.
     * @return - List<FileRes> with all the folders & documents to send.
     */
    @RequestMapping(value = "getAll", method = RequestMethod.GET)
    public ResponseEntity<List<FileRes>> getAll(@RequestParam(required = false) Long parentFolderId, @RequestAttribute Long userId) throws AccountNotFoundException {
        logger.info("in FileController -> getAll");
        return ac.getAll(parentFolderId, userId);
    }

    /**
     * createFolder is a request from the client to create a new folder in a specific location.
     * @param parentFolderId - create a folder inside this folder id.
     * @param name - name of the new folder.
     * @param userId - the user that creates the new folder.
     * @return ResponseEntity with a message.
     */
    @RequestMapping(value = "folder", method = RequestMethod.POST)
    public ResponseEntity<?> createFolder(@RequestParam(required = false) Long parentFolderId, @RequestParam String name, @RequestAttribute Long userId) {
        logger.info("in FileController -> createFolder");

        Folder parentFolder = null;
        try {
            if (parentFolderId != null) {
                parentFolder = folderService.findById(parentFolderId);
            }
            User user = userService.findById(userId);
            Folder folder = Folder.createFolder(name, parentFolder, user);
            return ac.create(folder, Folder.class);
        } catch (AccountNotFoundException e) {
            logger.error("in FileController -> createFolder --> "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    /**
     * createDocument is a request from the client to create a new document in a specific folder location.
     * @param parentFolderId- create a document inside this folder id.
     * @param name - name of the new folder.
     * @param content - the content of a document, with data if it was from import a file request.
     * @param userId - the user that creates the new folder
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> createDocument(@RequestParam Long parentFolderId, @RequestParam String name, @RequestBody(required = false) String content, @RequestAttribute Long userId) {
        logger.info("in FileController -> createDocument");
        try {
            Folder parentFolder = folderService.findById(parentFolderId);
            User user = userService.findById(userId);
            Document doc = Document.createDocument(user, name, parentFolder, content != null ? content : "");
            return ac.create(doc, Document.class);
        } catch (AccountNotFoundException e) {
            logger.error("in FileController -> createDocument --> "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * renameFolder is a request from the client to change a given folder's name.
     * @param folderId - the folder that will change its name.
     * @param name - new name.
     * @param userId - user that sends this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "folder/rename", method = RequestMethod.PATCH)
    public ResponseEntity<?> renameFolder(@RequestParam Long folderId, @RequestParam String name, @RequestAttribute Long userId) {
        logger.info("in FileController -> renameFolder");
        if(name==null || name.length()==0 ){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionMessage.VALIDATION_FAILED+"name is empty/null");
        }
        return ac.rename(folderId, name, Folder.class);
    }

    /**
     * renameDocument is a request from the client to change a given document's name.
     * @param documentId -  the document that will change its name.
     * @param name -  new name.
     * @param userId - user that sends this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document/rename", method = RequestMethod.PATCH)
    public ResponseEntity<?> renameDocument(@RequestParam Long documentId, @RequestParam String name, @RequestAttribute Long userId) {
        logger.info("in FileController -> renameDocument");
        if(name==null || name.length()==0 ){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionMessage.VALIDATION_FAILED+"name is empty/null");
        }
        return ac.rename(documentId, name, Document.class);
    }

    /**
     * deleteFolder is a DELETE method that called from the client to delete a folder.
     * all the folder's content (documents and inner folders) will be deleted also.
     * @param folderId - the folder that will be deleted.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "folder", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteFolder(@RequestParam Long folderId, @RequestAttribute Long userId) {
        logger.info("in FileController -> deleteFolder");
        return ac.delete(folderId, Folder.class);
    }

    /**
     * deleteDocument is a DELETE method that called from the client to delete a document.
     * all the document's content will be deleted also.
     * @param documentId - the document that will be deleted.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteDocument(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> deleteDocument");
        return ac.delete(documentId, Document.class);
    }

    /**
     *  relocateFolder is a PATCH method that called from the client to relocate a folder's location.
     * @param newParentFolderId - the new location of the folder.
     * @param folderId - what folder we need to relocate.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "folder/relocate", method = RequestMethod.PATCH)
    public ResponseEntity<?> relocateFolder(@RequestParam Long newParentFolderId, @RequestParam Long folderId, @RequestAttribute Long userId) {
        logger.info("in FileController -> relocateFolder");
        try {
            Folder folder = folderService.findById(folderId);
            return ac.relocate(newParentFolderId, folderId, Folder.class);
        } catch (AccountNotFoundException e) {
            logger.error("in FileController -> relocateFolder --> "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * relocateDocument is a PATCH method that called from the client to relocate a document's location.
     * @param newParentFolderId - the new location of the document.
     * @param documentId - what document we need to relocate.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document/relocate", method = RequestMethod.PATCH)
    public ResponseEntity<?> relocateDocument(@RequestParam Long newParentFolderId, @RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> relocateDocument");
        try {
            Document doc = documentService.findById(documentId);
            return ac.relocate(newParentFolderId, documentId, Document.class);
        } catch (AccountNotFoundException e) {
            logger.error("in FileController -> relocateDocument --> "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * export is a GET method that called from the client to export a document content onto a file.
     * @param documentId - the document that will be exported.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document/export", method = RequestMethod.GET)
    public ResponseEntity<?> export(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> export");
        try {
            Document document = documentService.findById(documentId);
            ExportDoc exportDoc = new ExportDoc(document.getName(), document.getContent());
            return ResponseEntity.ok().body(exportDoc);
        } catch (AccountNotFoundException e) {
            logger.error("in FileController -> export --> "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * getPath is a GET method that called from the client when we enter a folder inside the client side,
     * and want to present the client the new path he has done so far.
     * @param type - enum of FOLDER or DOCUMENT
     * @param fileId - id of either FOLDER or DOCUMENT
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "getPath", method = RequestMethod.GET)
    public ResponseEntity<?> getPath(@RequestParam Type type, @RequestParam Long fileId, @RequestAttribute Long userId) {
        logger.info("in FileController -> getPath");
        List<FileRes> path = new ArrayList<>();
        GeneralItem generalItem = null;
        try {
            switch (type) {
                case FOLDER:
                    generalItem = folderService.findById(fileId);
                    break;
                case DOCUMENT:
                    generalItem = documentService.findById(fileId);
                    break;
            }
            Folder parentFolder = generalItem.getParentFolder();
            if(type.equals(Type.FOLDER)){
                path.add(0, new FileRes(generalItem.getName(), generalItem.getId(), Type.FOLDER));
            }
            while (parentFolder != null) {
                path.add(0, new FileRes(parentFolder.getName(), parentFolder.getId(), Type.FOLDER));
                parentFolder = parentFolder.getParentFolder();
            }
            return ResponseEntity.ok(path);
        } catch (AccountNotFoundException e) {
            logger.error("in FileController -> getPath --> "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * documentExists is a get method that checks if a given document id is exist in our database.
     * @param documentId - the document id to search.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document/isExists", method = RequestMethod.GET)
    public ResponseEntity<?> documentExists(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> documentExists");
        try {
            return ResponseEntity.ok(documentService.findById(documentId));
        } catch (AccountNotFoundException e) {
            logger.error("in FileController -> documentExists --> "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * getUser is a POST? method that sends back to the client an entity of JoinRes which contain
     *  name; userId; permission;
     * @param documentId - the document we work on.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document/getUser", method = RequestMethod.POST)
    public ResponseEntity<?> getUser(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> getUser");
        try {
            User user = userService.findById(userId);
            Permission permission = documentService.getUserPermissionInDocument(userId, documentId);
            return ResponseEntity.ok(new JoinRes(user.getName(), userId, permission));
        } catch (AccountNotFoundException e) {
            logger.error("in FileController -> getUser --> "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * getContent is a GET method that the client needs to show content of a file when a file is loading.
     * @param documentId - document id in database.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "/document/getContent", method = RequestMethod.GET)
    public ResponseEntity<String> getContent(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> getContent");
        String content = documentService.getContent(documentId);
        return ResponseEntity.ok().body(content);
    }

    /**
     * getDocumentName is a GET method to show the document name when a new document is opend in the client side.
     * @param documentId - document id in database.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "/document/name", method = RequestMethod.GET)
    public ResponseEntity<String> getDocumentName(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> getDocumentName");
        try {
            Document document=documentService.findById(documentId);
            return ResponseEntity.ok(document.getName());
        } catch (AccountNotFoundException e) {
            logger.error("in FileController -> getContent -> "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}