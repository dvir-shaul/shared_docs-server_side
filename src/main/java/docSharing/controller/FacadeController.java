package docSharing.controller;

import docSharing.entity.*;
import docSharing.requests.Type;
import docSharing.response.*;
import docSharing.service.*;
import docSharing.utils.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.security.auth.login.AccountNotFoundException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class FacadeController {

    @Autowired
    DocumentService documentService;
    @Autowired
    FolderService folderService;
    @Autowired
    UserService userService;
    @Autowired
    AuthService authService;

    public Response getAll(Long parentFolderId, Long userId) {
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
            // TODO: we need to throw more exceptions so we know what status to retrieve!
            return new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message(e.getMessage())
                    .statusCode(400)
                    .build();
        }
    }

    public Response create(Long parentFolderId, String name, String content, Long userId, Class c) {
        try {
            Validations.validate(Regex.FILE_NAME.getRegex(), name);
//            Validations.validate(Regex.ID.getRegex(), item.getParentFolderId().toString());
            Folder parentFolder = null;
            if (parentFolderId != null)
                parentFolder = folderService.findById(parentFolderId);
            User user = userService.findById(userId);
            return new Response.Builder()
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .data(convertFromClassToService(c).create(parentFolder, user, name, content))
                    .message("item created successfully")
                    .build();

        } catch (NullPointerException | IllegalArgumentException | FileNotFoundException | AccountNotFoundException e) {
            return new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .message(e.getMessage())
                    .build();
        }
    }

    public Response getPath(Long itemId, Class c) {
        return new Response.Builder()
                .status(HttpStatus.OK)
                .statusCode(200)
                .message("Successfully managed to retrieve path")
                .data(convertFromClassToService(c).getPath(itemId))
                .build();
    }

    public Response rename(Long id, String name, Class c) {
        // FIXME: need to validate name using Validations.validate!
        //  it also checks if null and returns an exception so we need to catch it here.
        if (name == null)
            return new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .message("You must include all and exact parameters for such an action: name")
                    .build();

        return new Response.Builder()
                .status(HttpStatus.OK)
                .statusCode(200)
                .message("Successfully renamed to: " + convertFromClassToService(c).rename(id, name))
                .build();
    }

    public Response delete(Long id, Class c) {
        // FIXME: We have this check in a Validation.validate function. Why not do that there and let it throw its exception?
        //  if we do it this way, we only return an exception response once, and not twice.
        if (id == null)
            return new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .message("I'm sorry. In order for me to delete a document, you need to be more specific about its id... So what is it's id?")
                    .build();
        try {
            convertFromClassToService(c).delete(id);
            return new Response.Builder()
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .message("An item answering to the id:" + id + " has been successfully erased from the database!")
                    .build();

        } catch (FileNotFoundException e) {
            return new Response.Builder()
                    .status(HttpStatus.NOT_FOUND)
                    .statusCode(400)
                    .message("You must include all and exact parameters for such an action: id")
                    .build();
        }
    }

    public Response relocate(Long newParentId, Long id, Class c) {
        try {
            if (id == null)
                return new Response.Builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .statusCode(400)
                        .message("In order to relocate, an ID must be provided!")
                        .build();

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

        } catch (FileNotFoundException e) {
            return new Response.Builder()
                    .message(e.getMessage())
                    .statusCode(400)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    public Response export(Long documentId) {
        try {
            Document document = documentService.findById(documentId);
            ExportDoc exportDoc = new ExportDoc(document.getName(), document.getContent());
            return new Response.Builder()
                    .data(exportDoc)
                    .status(HttpStatus.OK)
                    .message("Export performed successfully")
                    .build();

        } catch (FileNotFoundException e) {
            return new Response.Builder()
                    .message(e.getMessage())
                    .statusCode(400)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    public Response doesExist(Long id, Class c) {
        if (id == null)
            return new Response.Builder()
                    .message("File of type " + c.getSimpleName() + " with an id of: " + id + " does not exist")
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .build();

        return new Response.Builder()
                .status(HttpStatus.OK)
                .message("File exists")
                .statusCode(200)
                .data(convertFromClassToService(c).doesExist(id))
                .build();
    }

    public Response getContent(Long documentId) {
        return new Response.Builder()
                .status(HttpStatus.OK)
                .message("Successfully managed to retrieve the document's content")
                .statusCode(200)
                .data(documentService.getContent(documentId))
                .build();
    }

    public Response getDocumentName(Long documentId) {
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
                    .statusCode(401)
                    .build();
        }

    }















    /**
     * This function gets an item as a parameter and extracts its class in order to return the correct service.
     *
     * @param c - class of folder/document
     * @return the service we need to use according to what file it is.
     */
    private ServiceInterface convertFromClassToService(Class c) {
        if (c.equals(Document.class)) return documentService;
        if (c.equals(Folder.class)) return folderService;
        return null;
    }

    private List<FileRes> convertToFileRes(List<Folder> folders, List<Document> documents) {
        List<FileRes> fileResList = new ArrayList<>();

        for (Folder folder : folders)
            fileResList.add(new FileRes(folder.getName(), folder.getId(), Type.FOLDER, Permission.ADMIN, folder.getUser().getEmail()));

        for (Document document : documents)
            fileResList.add(new FileRes(document.getName(), document.getId(), Type.DOCUMENT, Permission.ADMIN, document.getUser().getEmail()));

        return fileResList;
    }
}