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
        Response response = facadeController.getContent(documentId);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @RequestMapping(value = "document", method = RequestMethod.GET)
    public ResponseEntity<Response> getDocumentName(@RequestParam Long documentId, @RequestAttribute Long userId) {
        Response response = facadeController.getDocumentName(documentId);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @RequestMapping(value = "folder", method = RequestMethod.POST)
    public ResponseEntity<Response> createFolder(@RequestParam(required = false) Long parentFolderId, @RequestParam String name, @RequestBody(required = false) String content, @RequestAttribute Long userId) {
        Response response = facadeController.create(parentFolderId, name, content, userId, Folder.class);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @RequestMapping(value = "document", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Response> createDocument(@RequestParam Long parentFolderId, @RequestParam String name, @RequestBody(required = false) String content, @RequestAttribute Long userId) {
        Response response = facadeController.create(parentFolderId, name, content, userId, Document.class);
        return new ResponseEntity<>(response, response.getStatus());
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