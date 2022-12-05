package docSharing.controller;

import docSharing.entity.*;
import docSharing.requests.*;
import docSharing.response.*;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping(value = "/file")
@CrossOrigin
@AllArgsConstructor
@NoArgsConstructor
class FileController {
    private static Logger logger = LogManager.getLogger(FileController.class.getName());

    @Autowired
    FacadeController facadeController;
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
    public ResponseEntity<Response> getAll(@RequestParam(required = false) Long parentFolderId, @RequestAttribute Long userId) throws AccountNotFoundException {
        logger.info("in FileController -> getAll");
        Response response = facadeController.getAll(parentFolderId, userId);
        return new ResponseEntity<>(response, response.getStatus());
    }
    /**
     * renameFolder is a request from the client to change a given folder's name.
     * @param folderId - the folder that will change its name.
     * @param name - new name.
     * @param userId - user that sends this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "folder/rename", method = RequestMethod.PATCH)
    public ResponseEntity<Response> renameFolder(@RequestParam Long folderId, @RequestParam String name, @RequestAttribute Long userId) {
        logger.info("in FileController -> renameFolder");
        Response response = facadeController.rename(folderId, name, Folder.class);
        return new ResponseEntity<>(response, response.getStatus());
    }
    /**
     * deleteFolder is a DELETE method that called from the client to delete a folder.
     * all the folder's content (documents and inner folders) will be deleted also.
     * @param folderId - the folder that will be deleted.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "folder", method = RequestMethod.DELETE)
    public ResponseEntity<Response> deleteFolder(@RequestParam Long folderId, @RequestAttribute Long userId) {
        logger.info("in FileController -> deleteFolder");
        Response response = facadeController.delete(folderId, Folder.class);
        return new ResponseEntity<>(response, response.getStatus());
    }
    /**
     * deleteDocument is a DELETE method that called from the client to delete a document.
     * all the document's content will be deleted also.
     * @param documentId - the document that will be deleted.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document", method = RequestMethod.DELETE)
    public ResponseEntity<Response> deleteDocument(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> deleteDocument");
        Response response = facadeController.delete(documentId, Document.class);
        return new ResponseEntity<>(response, response.getStatus());
    }
    /**
     *  relocateFolder is a PATCH method that called from the client to relocate a folder's location.
     * @param newParentFolderId - the new location of the folder.
     * @param folderId - what folder we need to relocate.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "folder/relocate", method = RequestMethod.PATCH)
    public ResponseEntity<Response> relocateFolder(@RequestParam Long newParentFolderId, @RequestParam Long folderId, @RequestAttribute Long userId) {
        logger.info("in FileController -> relocateFolder");
        Response response = facadeController.relocate(newParentFolderId, folderId, Folder.class);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * relocateDocument is a PATCH method that called from the client to relocate a document's location.
     * @param newParentFolderId - the new location of the document.
     * @param documentId - what document we need to relocate.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document/relocate", method = RequestMethod.PATCH)
    public ResponseEntity<Response> relocateDocument(@RequestParam Long newParentFolderId, @RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> relocateDocument");
        Response response = facadeController.relocate(newParentFolderId, documentId, Document.class);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * export is a GET method that called from the client to export a document content onto a file.
     * @param documentId - the document that will be exported.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document/export", method = RequestMethod.GET)
    public ResponseEntity<Response> export(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> export");
        Response response = facadeController.export(documentId);
        return new ResponseEntity<>(response, response.getStatus());

    }

    /**
     * doesDocumentExists is a get method that checks if a given document id is exist in our database.
     * @param documentId - the document id to search.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document/doesExists", method = RequestMethod.GET)
    public ResponseEntity<Response> doesDocumentExists(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> doesDocumentExists");
        Response response = facadeController.doesExist(documentId, Document.class);
        return new ResponseEntity<>(response, response.getStatus());

    }

    /**
     * doesFolderExists is a get method that checks if a given document id is exist in our database.
     * @param folderId - the folder id to search.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */

    @RequestMapping(value = "folder/doesExists", method = RequestMethod.GET)
    public ResponseEntity<Response> doesFolderExists(@RequestParam Long folderId, @RequestAttribute Long userId) {
        logger.info("in FileController -> doesFolderExists");
        Response response = facadeController.doesExist(folderId, Folder.class);
        return new ResponseEntity<>(response, response.getStatus());

    }

    /**
     * renameDocument is a request from the client to change a given document's name.
     * @param documentId -  the document that will change its name.
     * @param name -  new name.
     * @param userId - user that sends this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document/rename", method = RequestMethod.PATCH)
    public ResponseEntity<Response> renameDocument(@RequestParam Long documentId, @RequestParam String name, @RequestAttribute Long userId) {
        logger.info("in FileController -> renameDocument");
        Response response = facadeController.rename(documentId, name, Document.class);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * getDocumentName
     * @param documentId - doc id
     * @param userId - user id
     * @return -
     */
    @RequestMapping(value = "document", method = RequestMethod.GET)
    public ResponseEntity<Response> getDocumentName(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> getDocumentName");
        try {
            Document document = documentService.findById(documentId);
            FileRes fileResponse = new FileRes(document.getName(), document.getId(), Type.DOCUMENT, Permission.ADMIN, document.getUser().getEmail());
            return new ResponseEntity<>(new Response.Builder()
                    .statusCode(200)
                    .status(HttpStatus.OK)
                    .data(fileResponse)
                    .message("Managed to get file name properly")
                    .build(), HttpStatus.OK);

        } catch (FileNotFoundException e) {
            return new ResponseEntity<>(new Response.Builder()
                    .message("Couldn't find such a file " + e)
                    .status(HttpStatus.NOT_FOUND)
                    .statusCode(401)
                    .build(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * createFolder is a request from the client to create a new folder in a specific location.
     * @param parentFolderId - create a folder inside this folder id.
     * @param name - name of the new folder.
     * @param userId - the user that creates the new folder.
     * @return ResponseEntity with a message.
     */
    @RequestMapping(value = "folder", method = RequestMethod.POST)
    public ResponseEntity<Response> createFolder(@RequestParam(required = false) Long parentFolderId, @RequestParam String name, @RequestAttribute Long userId) {
        logger.info("in FileController -> createFolder");
        Folder parentFolder = null;
        try {
            if (parentFolderId != null) {
                parentFolder = folderService.findById(parentFolderId);
            }
            User user = userService.findById(userId);
            Folder folder = Folder.createFolder(name, parentFolder, user);
            return new ResponseEntity<>(new Response.Builder()
                    .status(HttpStatus.CREATED)
                    .statusCode(200)
                    .message("Created successfully!")
                    .data(facadeController.create(folder, Folder.class))
                    .build(), HttpStatus.CREATED);

        } catch (AccountNotFoundException | FileNotFoundException e) {
            logger.error("in FileController -> createFolder --> "+e.getMessage());
            return new ResponseEntity<>(new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .message("Could not create a folder!")
                    .build(), HttpStatus.BAD_REQUEST);
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
    public ResponseEntity<Response> createDocument(@RequestParam Long parentFolderId, @RequestParam String name, @RequestBody(required = false) String content, @RequestAttribute Long userId) {
        logger.info("in FileController -> createDocument");
        try {
            Folder parentFolder = folderService.findById(parentFolderId);
            User user = userService.findById(userId);
            Document doc = Document.createDocument(user, name, parentFolder, content != null ? content : "");
            Response response = facadeController.create(doc, Document.class);
            return new ResponseEntity<>(response, response.getStatus());

        } catch (FileNotFoundException | AccountNotFoundException e) {
            logger.error("in FileController -> createDocument --> "+e.getMessage());
            return new ResponseEntity<>(new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .message(e.getMessage())
                    .build(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * getPath is a GET method that called from the client when we enter a folder inside the client side,
     * and want to present the client the new path he has done so far.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document/getPath", method = RequestMethod.GET)
    public ResponseEntity<Response> getDocumentPath(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> getDocumentPath");
        try {
            Document document = documentService.findById(documentId);
            Response response = facadeController.getPath(document, Document.class);
            return new ResponseEntity<>(response, response.getStatus());

        } catch (FileNotFoundException e) {
            return new ResponseEntity<>(new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message(e.getMessage())
                    .build(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * getPath is a GET method that called from the client when we enter a folder inside the client side,
     * and want to present the client the new path he has done so far.
     * @param userId - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "folder/getPath", method = RequestMethod.GET)
    public ResponseEntity<Response> getFolderPath(@RequestParam Long folderId, @RequestAttribute Long userId) {
        logger.info("in FileController -> getFolderPath");
        try {
            Folder folder = folderService.findById(folderId);
            Response response = facadeController.getPath(folder, Folder.class);
            return new ResponseEntity<>(response, response.getStatus());
        } catch (FileNotFoundException e) {
            return new ResponseEntity<>(new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .message(e.getMessage())
                    .build(), HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * getContent
     * @param documentId - doc id
     * @param userId - user id
     * @return -
     */
    @RequestMapping(value = "document/getContent", method = RequestMethod.GET)
    public ResponseEntity<Response> getContent(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> getContent");
        // FIXME: What if the document doesn't exist?
        return new ResponseEntity<>(new Response.Builder()
                .data(documentService.getContent(documentId))
                .statusCode(200)
                .data(HttpStatus.OK)
                .message("Successfully managed to retrieve the document's content")
                .build(), HttpStatus.OK);
    }


}