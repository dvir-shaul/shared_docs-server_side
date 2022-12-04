package docSharing.controller;

import docSharing.entity.*;
import docSharing.requests.*;
import docSharing.response.*;
import docSharing.service.DocumentService;
import docSharing.service.FolderService;
import docSharing.service.UserService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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

    @Autowired
    FacadeController facadeController;
    @Autowired
    FolderService folderService;
    @Autowired
    DocumentService documentService;
    @Autowired
    UserService userService;

    @RequestMapping(value = "getAll", method = RequestMethod.GET)
    public ResponseEntity<Response> getAll(@RequestParam(required = false) Long parentFolderId, @RequestAttribute Long userId) throws AccountNotFoundException {
        Response response = facadeController.getAll(parentFolderId, userId);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @RequestMapping(value = "folder/rename", method = RequestMethod.PATCH)
    public ResponseEntity<Response> renameFolder(@RequestParam Long folderId, @RequestParam String name, @RequestAttribute Long userId) {
        Response response = facadeController.rename(folderId, name, Folder.class);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @RequestMapping(value = "document/rename", method = RequestMethod.PATCH)
    public ResponseEntity<Response> renameDocument(@RequestParam Long documentId, @RequestParam String name, @RequestAttribute Long userId) {
        Response response = facadeController.rename(documentId, name, Document.class);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @RequestMapping(value = "folder", method = RequestMethod.DELETE)
    public ResponseEntity<Response> deleteFolder(@RequestParam Long folderId, @RequestAttribute Long userId) {
        Response response = facadeController.delete(folderId, Folder.class);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @RequestMapping(value = "document", method = RequestMethod.DELETE)
    public ResponseEntity<Response> deleteDocument(@RequestParam Long documentId, @RequestAttribute Long userId) {
        Response response = facadeController.delete(documentId, Document.class);
        return new ResponseEntity<>(response, response.getStatus());

    }

    @RequestMapping(value = "folder/relocate", method = RequestMethod.PATCH)
    public ResponseEntity<Response> relocateFolder(@RequestParam Long newParentFolderId, @RequestParam Long folderId, @RequestAttribute Long userId) {
        Response response = facadeController.relocate(newParentFolderId, folderId, Folder.class);
        return new ResponseEntity<>(response, response.getStatus());


    }

    @RequestMapping(value = "document/relocate", method = RequestMethod.PATCH)
    public ResponseEntity<Response> relocateDocument(@RequestParam Long newParentFolderId, @RequestParam Long documentId, @RequestAttribute Long userId) {
        Response response = facadeController.relocate(newParentFolderId, documentId, Document.class);
        return new ResponseEntity<>(response, response.getStatus());

    }

    @RequestMapping(value = "document/export", method = RequestMethod.GET)
    public ResponseEntity<Response> export(@RequestParam Long documentId, @RequestAttribute Long userId) {
        Response response = facadeController.export(documentId);
        return new ResponseEntity<>(response, response.getStatus());

    }


    @RequestMapping(value = "document/doesExists", method = RequestMethod.GET)
    public ResponseEntity<Response> doesDocumentExists(@RequestParam Long documentId, @RequestAttribute Long userId) {
        Response response = facadeController.doesExist(documentId, Document.class);
        return new ResponseEntity<>(response, response.getStatus());

    }

    @RequestMapping(value = "folder/doesExists", method = RequestMethod.GET)
    public ResponseEntity<Response> doesFolderExists(@RequestParam Long folderId, @RequestAttribute Long userId) {
        Response response = facadeController.doesExist(folderId, Folder.class);
        return new ResponseEntity<>(response, response.getStatus());

    }

    @RequestMapping(value = "document/getContent", method = RequestMethod.GET)
    public ResponseEntity<Response> getContent(@RequestParam Long documentId, @RequestAttribute Long userId) {
        // FIXME: What if the document doesn't exist?
        return new ResponseEntity<>(new Response.Builder()
                .data(documentService.getContent(documentId))
                .statusCode(200)
                .data(HttpStatus.OK)
                .message("Successfully managed to retrieve the document's content")
                .build(), HttpStatus.OK);
    }

    @RequestMapping(value = "document", method = RequestMethod.GET)
    public ResponseEntity<Response> getDocumentName(@RequestParam Long documentId, @RequestAttribute Long userId) {
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

    @RequestMapping(value = "folder", method = RequestMethod.POST)
    public ResponseEntity<Response> createFolder(@RequestParam(required = false) Long parentFolderId, @RequestParam String name, @RequestAttribute Long userId) {
        Folder parentFolder = null;
        try {
            if (parentFolderId != null) {
                parentFolder = folderService.findById(parentFolderId);
            }
            User user = userService.findById(userId);
            Folder folder = Folder.createFolder(name, parentFolder, user);
//            FileRes folderResponse = new FileRes(name, null, Type.FOLDER, Permission.ADMIN, user.getEmail());

            return new ResponseEntity<>(new Response.Builder()
                    .status(HttpStatus.CREATED)
                    .statusCode(200)
                    .message("Created successfully!")
                    .data(facadeController.create(folder, Folder.class))
                    .build(), HttpStatus.CREATED);

        } catch (AccountNotFoundException | FileNotFoundException e) {
            return new ResponseEntity<>(new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .message("Could not create a folder!")
                    .build(), HttpStatus.BAD_REQUEST);
        }

    }

    @RequestMapping(value = "document", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Response> createDocument(@RequestParam Long parentFolderId, @RequestParam String name, @RequestBody(required = false) String content, @RequestAttribute Long userId) {
        try {
            Folder parentFolder = folderService.findById(parentFolderId);
            User user = userService.findById(userId);
            Document doc = Document.createDocument(user, name, parentFolder, content != null ? content : "");
            Response response = facadeController.create(doc, Document.class);
            return new ResponseEntity<>(response, response.getStatus());

        } catch (FileNotFoundException | AccountNotFoundException e) {
            return new ResponseEntity<>(new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .message(e.getMessage())
                    .build(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "document/getPath", method = RequestMethod.GET)
    public ResponseEntity<Response> getDocumentPath(@RequestParam Long documentId, @RequestAttribute Long userId) {
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

    @RequestMapping(value = "folder/getPath", method = RequestMethod.GET)
    public ResponseEntity<Response> getFolderPath(@RequestParam Long folderId, @RequestAttribute Long userId) {
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
}