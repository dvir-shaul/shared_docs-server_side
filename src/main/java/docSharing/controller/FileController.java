package docSharing.controller;

import docSharing.entity.*;
import docSharing.response.*;
import docSharing.service.DocumentService;
import docSharing.service.FolderService;
import docSharing.service.UserService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;


@Controller
@RequestMapping(value = "/file")
@CrossOrigin
@AllArgsConstructor
@NoArgsConstructor
class FileController {
    private static Logger logger = LogManager.getLogger(FileController.class.getName());

    @Autowired
    FacadeFileController facadeFileController;
    @Autowired
    FolderService folderService;
    @Autowired
    DocumentService documentService;
    @Autowired
    UserService userService;

    /**
     * getAll function called from the client when we enter a new folder, and it should send the client a list with all
     * the folders & documents to present the client.
     *
     * @param parentFolderId - folder id.
     * @param userId         - the user id.
     * @return - List<FileRes> with all the folders & documents to send.
     */
    @RequestMapping(value = "getAll", method = RequestMethod.GET)
    public ResponseEntity<Response> getAll(@RequestParam(required = false) Long parentFolderId, @RequestAttribute Long userId) throws AccountNotFoundException {
        logger.info("in FileController -> getAll");
        Response response = facadeFileController.getAll(parentFolderId, userId);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * renameFolder is a request from the client to change a given folder's name.
     *
     * @param folderId - the folder that will change its name.
     * @param name     - new name.
     * @param userId   - user that sends this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "folder/rename", method = RequestMethod.PATCH)
    public ResponseEntity<Response> renameFolder(@RequestParam Long folderId, @RequestParam String name, @RequestAttribute Long userId) {
        logger.info("in FileController -> renameFolder");
        Response response = facadeFileController.rename(folderId, name, Folder.class);
        return new ResponseEntity<>(response, response.getStatus());
    }


    /**
     * renameDocument is a request from the client to change a given document's name.
     *
     * @param documentId -  the document that will change its name.
     * @param name       -  new name.
     * @param userId     - user that sends this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document/rename", method = RequestMethod.PATCH)
    public ResponseEntity<Response> renameDocument(@RequestParam Long documentId, @RequestParam String name, @RequestAttribute Long userId) {
        logger.info("in FileController -> renameDocument");
        Response response = facadeFileController.rename(documentId, name, Document.class);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * deleteFolder is a DELETE method that called from the client to delete a folder.
     * all the folder's content (documents and inner folders) will be deleted also.
     *
     * @param folderId - the folder that will be deleted.
     * @param userId   - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "folder", method = RequestMethod.DELETE)
    public ResponseEntity<Response> deleteFolder(@RequestParam Long folderId, @RequestAttribute Long userId) {
        logger.info("in FileController -> deleteFolder");
        Response response = facadeFileController.delete(folderId, Folder.class);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * deleteDocument is a DELETE method that called from the client to delete a document.
     * all the document's content will be deleted also.
     *
     * @param documentId - the document that will be deleted.
     * @param userId     - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document", method = RequestMethod.DELETE)
    public ResponseEntity<Response> deleteDocument(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> deleteDocument");
        Response response = facadeFileController.delete(documentId, Document.class);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * relocateFolder is a PATCH method that called from the client to relocate a folder's location.
     *
     * @param newParentFolderId - the new location of the folder.
     * @param folderId          - what folder we need to relocate.
     * @param userId            - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "folder/relocate", method = RequestMethod.PATCH)
    public ResponseEntity<Response> relocateFolder(@RequestParam Long newParentFolderId, @RequestParam Long folderId, @RequestAttribute Long userId) {
        logger.info("in FileController -> relocateFolder");
        Response response = facadeFileController.relocate(newParentFolderId, folderId, Folder.class);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * relocateDocument is a PATCH method that called from the client to relocate a document's location.
     *
     * @param newParentFolderId - the new location of the document.
     * @param documentId        - what document we need to relocate.
     * @param userId            - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document/relocate", method = RequestMethod.PATCH)
    public ResponseEntity<Response> relocateDocument(@RequestParam Long newParentFolderId, @RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> relocateDocument");
        Response response = facadeFileController.relocate(newParentFolderId, documentId, Document.class);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * export is a GET method that called from the client to export a document content onto a file.
     *
     * @param documentId - the document that will be exported.
     * @param userId     - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document/export", method = RequestMethod.GET)
    public ResponseEntity<Response> export(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> export");
        Response response = facadeFileController.export(documentId);
        return new ResponseEntity<>(response, response.getStatus());

    }

    /**
     * doesDocumentExists is a get method that checks if a given document id is exist in our database.
     *
     * @param documentId - the document id to search.
     * @param userId     - the user that creates this request.
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document/doesExists", method = RequestMethod.GET)
    public ResponseEntity<Response> doesDocumentExists(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> doesDocumentExists");
        Response response = facadeFileController.doesExist(documentId, Document.class);
        return new ResponseEntity<>(response, response.getStatus());

    }

    /**
     * doesFolderExists is a get method that checks if a given document id is exist in our database.
     *
     * @param folderId - the folder id to search.
     * @param userId   - the user that creates this request.
     * @return - ResponseEntity with a message.
     */

    @RequestMapping(value = "folder/doesExists", method = RequestMethod.GET)
    public ResponseEntity<Response> doesFolderExists(@RequestParam Long folderId, @RequestAttribute Long userId) {
        logger.info("in FileController -> doesFolderExists");
        Response response = facadeFileController.doesExist(folderId, Folder.class);
        return new ResponseEntity<>(response, response.getStatus());

    }


    /**
     * getDocumentName
     *
     * @param documentId - doc id
     * @param userId     - user id
     * @return -
     */
    @RequestMapping(value = "document", method = RequestMethod.GET)
    public ResponseEntity<Response> getDocumentName(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> getDocumentName");
        Response response = facadeFileController.getDocumentName(documentId);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * createFolder is a request from the client to create a new folder in a specific location.
     *
     * @param parentFolderId - create a folder inside this folder id.
     * @param name           - name of the new folder.
     * @param userId         - the user that creates the new folder.
     * @return ResponseEntity with a message.
     */
    @RequestMapping(value = "folder", method = RequestMethod.POST)
    public ResponseEntity<Response> createFolder(@RequestParam(required = false) Long parentFolderId, @RequestParam String name, @RequestBody(required = false) String content, @RequestAttribute Long userId) {
        logger.info("in FileController -> createFolder");
        Response response = facadeFileController.create(parentFolderId, name, content, userId, Folder.class);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * createDocument is a request from the client to create a new document in a specific folder location.
     *
     * @param parentFolderId- create a document inside this folder id.
     * @param name            - name of the new folder.
     * @param content         - the content of a document, with data if it was from import a file request.
     * @param userId          - the user that creates the new folder
     * @return - ResponseEntity with a message.
     */
    @RequestMapping(value = "document", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Response> createDocument(@RequestParam Long parentFolderId, @RequestParam String name, @RequestBody(required = false) String content, @RequestAttribute Long userId) {
        logger.info("in FileController -> createDocument");
        Response response = facadeFileController.create(parentFolderId, name, content, userId, Document.class);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * getPath is a GET method that called from the client when we enter a document inside the client side,
     * and want to present the client the new path he has done so far.
     *
     * @param userId - the user that creates this request.
     * @return - ResponseEntity<Response> with a status code and the path.
     */
    @RequestMapping(value = "document/getPath", method = RequestMethod.GET)
    public ResponseEntity<Response> getDocumentPath(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> getDocumentPath");
        Response response = facadeFileController.getPath(documentId, Document.class);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * getPath is a GET method that called from the client when we enter a folder inside the client side,
     * and want to present the client the new path he has done so far.
     *
     * @param userId - the user that creates this request.
     * @return - ResponseEntity<Response> with a status code and the path.
     */
    @RequestMapping(value = "folder/getPath", method = RequestMethod.GET)
    public ResponseEntity<Response> getFolderPath(@RequestParam Long folderId, @RequestAttribute Long userId) {
        logger.info("in FileController -> getFolderPath");
        Response response = facadeFileController.getPath(folderId, Folder.class);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * getContent
     *
     * @param documentId - doc id
     * @param userId     - user id
     * @return -
     */
    @RequestMapping(value = "document/getContent", method = RequestMethod.GET)
    public ResponseEntity<Response> getContent(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in FileController -> getContent");
        Response response = facadeFileController.getContent(documentId);
        return new ResponseEntity<>(response, response.getStatus());
    }
}