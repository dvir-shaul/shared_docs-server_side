package docSharing.controller;

import docSharing.entity.*;
import docSharing.requests.Type;
import docSharing.response.ExportDoc;
import docSharing.response.FileRes;
import docSharing.response.Response;
import docSharing.service.DocumentService;
import docSharing.service.FolderService;
import docSharing.service.ServiceInterface;
import docSharing.service.UserService;
import docSharing.utils.ExceptionMessage;
import docSharing.utils.Regex;
import docSharing.utils.Validations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.security.auth.login.AccountNotFoundException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Component
public class FacadeFileController {
    private static Logger logger = LogManager.getLogger(FacadeFileController.class.getName());

    @Autowired
    private FolderService folderService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private UserService userService;


    /**
     * create in FacadeFileController get called from FileController when the client wants to create a
     * file of type Folder or Document
     *
     * @param parentFolderId- create a document inside this folder id.
     * @param name            - name of the new folder.
     * @param content         - the content of a document, with data if it was from import a file request.
     * @param userId          - the user that creates the new folder
     * @return - Response with id of the file as the data and status 200 if good or 400 if something went wrong
     */
    public Response create(Long parentFolderId, String name, String content, Long userId, Class c) {
        logger.info("in FacadeFileController -> create, item of Class:" + c);
        try {
            Validations.validate(Regex.FILE_NAME.getRegex(), name);
//            Validations.validate(Regex.ID.getRegex(), item.getParentFolderId().toString());
            Folder parentFolder = null;
            if (parentFolderId != null)
                parentFolder = folderService.findById(parentFolderId);
            User user = userService.findById(userId);
            return new Response.Builder()
                    .status(HttpStatus.CREATED)
                    .statusCode(201)
                    .data(convertFromClassToService(c).create(parentFolder, user, name, content))
                    .message("item created successfully")
                    .build();

        } catch (NullPointerException | IllegalArgumentException | FileNotFoundException | AccountNotFoundException e) {
            logger.error("in FacadeFileController -> create -> " + e.getMessage());
            return new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .message(e.getMessage())
                    .build();
        }

    }

    /**
     * getPath is a method that called from by FileController when we enter a folder or document inside the client side,
     * and want to present the client the new path he has done so far.
     *
     * @param itemId - folderId or documentId
     * @param c      - Folder or Document
     * @return - Response with the path to the current file we watch.
     */
    public Response getPath(Long itemId, Class c) {
        logger.info("in FacadeFileController -> create, item of Class:" + c);
        try {
            List<FileRes> path = convertFromClassToService(c).getPath(itemId);
            return new Response.Builder()
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .message("Successfully managed to retrieve path")
                    .data(path)
                    .build();
        } catch (FileNotFoundException e) {
            logger.error("in FacadeFileController -> create " + e.getMessage());
            return new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .message("item does not exist")
                    .data("")
                    .build();
        }
    }


    /**
     * getAll function called from the client when we enter a new folder, and it should send the client a list with all
     * the folders & documents to present the client.
     *
     * @param parentFolderId - folder id.
     * @param userId         - the user id.
     * @return response with a List<FileRes> with all the folders & documents to send.
     */
    public Response getAll(Long parentFolderId, Long userId) {
        logger.info("in FacadeFileController -> getAll, parentFolderId:" + parentFolderId + " userId:" + userId);
        try {
            List<Folder> folders;
            List<Document> documents;
            if (parentFolderId != null) {
                folders = folderService.get(parentFolderId, userId);
                documents = documentService.get(parentFolderId, userId);
            } else {
                folders = folderService.getAllWhereParentFolderIsNull(userId);
                documents = documentService.getAllWhereParentFolderIsNull(userId);
            }
            return new Response.Builder()
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .data(convertToFileRes(folders, documents))
                    .message("getAll function worked")
                    .build();

        } catch (AccountNotFoundException e) {
            logger.error("in FacadeController -> getAll -> AccountNotFoundException->" + e.getMessage());
            return new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message(e.getMessage())
                    .statusCode(400)
                    .build();
        } catch (FileNotFoundException e) {
            logger.error("in FacadeController -> getAll -> FileNotFoundException-> " + e.getMessage());
            return new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message(e.getMessage())
                    .statusCode(400)
                    .build();
        }
    }

    /**
     * rename a file, called from the fileController with a request to change name.
     *
     * @param id   - of a file.
     * @param name - new name.
     * @param c    - the class of the item, need to know to what service sends the request.
     * @return - ResponseEntity.
     */
    public Response rename(long id, String name, Class c) {
        logger.info("in FacadeController -> rename, id" + id + " of Class:" + c);
        try {
            Validations.validate(Regex.FILE_NAME.getRegex(), name);
            return new Response.Builder()
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .message("Successfully renamed to: " + convertFromClassToService(c).rename(id, name))
                    .build();

        } catch (IllegalArgumentException | FileNotFoundException e) {
            logger.error("in FacadeController -> rename -> name is not valid");
            return new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .message(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString())
                    .build();
        }

    }

    /**
     * delete a file, called from the fileController with a request to delete.
     * can be performed on either a Folder or Document.
     *
     * @param id - of a file to delete it.
     * @param c  - the class of the item, need to know to what service sends the request.
     * @return - ResponseEntity.
     */
    public Response delete(Long id, Class c) {
        logger.info("in FacadeController -> delete");
        try {
            Validations.validateIdNull(id);
            Validations.validate(Regex.ID.getRegex(), String.valueOf(id));
            convertFromClassToService(c).delete(id);
            return new Response.Builder()
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .message("An item answering to the id:" + id + " has been successfully erased from the database!")
                    .build();

        } catch (FileNotFoundException | IllegalArgumentException e) {
            logger.error("in FacadeController -> delete -> " + e.getMessage());
            return new Response.Builder()
                    .status(HttpStatus.NOT_FOUND)
                    .statusCode(400)
                    .message("You must include all and exact parameters for such an action: id")
                    .build();
        }
    }

    /**
     * relocate a file, called from the fileController with a request to relocate.
     *
     * @param newParentId - the new folder that the new file will insert into.
     * @param id          - of a file
     * @param c           - the class of the item, need to know to what service sends the request.
     * @return - ResponseEntity
     */
    public Response relocate(Long newParentId, long id, Class c) {
        logger.info("in FacadeFileController -> relocate, newParentId" + newParentId + " of Class:" + c);

        try {
            Validations.validate(Regex.ID.getRegex(), String.valueOf(id));
            Folder parentFolder = null;
            if (newParentId != null) {
                parentFolder = folderService.findById(newParentId);
            }
            return new Response.Builder()
                    .message("Successfully relocated a file")
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .data(convertFromClassToService(c).relocate(parentFolder, id))
                    .build();

        } catch (FileNotFoundException | IllegalArgumentException e) {
            logger.error("in FacadeController -> relocate -> " + e.getMessage());
            return new Response.Builder()
                    .message(e.getMessage())
                    .statusCode(400)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }


    /**
     * export get called by FileController when we need to export out a content of a document.
     *
     * @param documentId - document id in the database.
     * @return - ExportDoc entity that contain the name and the content of a document.
     */
    public Response export(Long documentId) {
        logger.info("in FacadeFileController -> export, documentId" + documentId);

        try {
            Document document = documentService.findById(documentId);
            ExportDoc exportDoc = new ExportDoc(document.getName(), document.getContent());
            return new Response.Builder()
                    .data(exportDoc)
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .message("Export performed successfully")
                    .build();

        } catch (FileNotFoundException e) {
            logger.error("in FacadeController -> export -> " + e.getMessage());
            return new Response.Builder()
                    .message(e.getMessage())
                    .statusCode(400)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    /**
     * doesExist get called by FileController when we need to check if an id of either a
     * Folder or Document exist in our database.
     *
     * @param id - id of the Folder or Document.
     * @param c  - class of the Folder or Document.
     * @return - a Response with status code and a Boolean.
     */
    public Response doesExist(long id, Class c) {
        logger.info("in FacadeController -> doesExist, id" + id + " of Class:" + c);

        return new Response.Builder()
                .status(HttpStatus.OK)
                .message("File exists")
                .statusCode(200)
                .data(convertFromClassToService(c).doesExist(id))
                .build();
    }

    /**
     * getContent function gets an documentId and gets the content of the document.
     * return the live document content from documentService documentsContentLiveChanges map.
     *
     * @param documentId - document id in data base.
     * @return - Response with the document content as the data.
     */
    public Response getContent(Long documentId) {
        logger.info("in FacadeFileController -> getContent, documentId:" + documentId);
        try {
            return new Response.Builder()
                    .status(HttpStatus.OK)
                    .message("Successfully managed to retrieve the document's content")
                    .statusCode(200)
                    .data(documentService.getContent(documentId))
                    .build();
        } catch (FileNotFoundException e) {
            return new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Successfully managed to retrieve the document's content")
                    .statusCode(400)
                    .build();
        }
    }

    /**
     * getDocumentName function gets an documentId and gets the name of the document.
     *
     * @param documentId - document id in data base.
     * @return - Response with the document name as the data.
     */
    public Response getDocumentName(long documentId) {
        logger.info("in FacadeFileController -> getDocumentName, documentId:" + documentId);
        Document document = null;
        try {
            document = documentService.findById(documentId);
            FileRes fileResponse = new FileRes(document.getName(), document.getId(), Type.DOCUMENT, Permission.ADMIN, document.getUser().getEmail());
            return new Response.Builder()
                    .statusCode(200)
                    .status(HttpStatus.OK)
                    .data(fileResponse)
                    .message("Managed to get file name properly")
                    .build();
        } catch (FileNotFoundException e) {
            return new Response.Builder()
                    .message("Couldn't find such a file " + e)
                    .status(HttpStatus.NOT_FOUND)
                    .statusCode(404)
                    .build();
        }

    }

    /**
     * This function gets an item as a parameter and extracts its class in order to
     * return the correct service we need to select the action we need.
     *
     * @param c - class of folder/document
     * @return the service we need to use according to what file it is.
     */

    private ServiceInterface convertFromClassToService(Class c) {
        logger.info("in FacadeFileController -> convertFromClassToService ,item of Class: " + c);

        if (c.equals(Document.class)) return documentService;
        if (c.equals(Folder.class)) return folderService;
        return null;
    }

    /**
     * convertToFileRes is an inner function of getAll, that gets a list of folders & documents,
     * and return a list of FileRes entity which has the name,id and the type of given file
     * for the convenient of the client side which need
     * to show all the files to user.
     *
     * @param folders   - List<Folder> to present.
     * @param documents - List<Document> to present
     * @return - List<FileRes>
     */
    private List<FileRes> convertToFileRes(List<Folder> folders, List<Document> documents) {
        logger.info("in FacadeFileController -> convertToFileRes");

        List<FileRes> fileResList = new ArrayList<>();

        for (Folder folder : folders)
            fileResList.add(new FileRes(folder.getName(), folder.getId(), Type.FOLDER, Permission.ADMIN, folder.getUser().getEmail()));

        for (Document document : documents)
            fileResList.add(new FileRes(document.getName(), document.getId(), Type.DOCUMENT, Permission.ADMIN, document.getUser().getEmail()));

        return fileResList;
    }
}
