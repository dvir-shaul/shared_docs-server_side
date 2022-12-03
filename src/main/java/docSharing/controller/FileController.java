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
    // FIXME: there's no need for folderService since "AbstractController" does all the logic.
    @Autowired
    FolderService folderService;
    // FIXME: there's no need for folderService since "DocumentController" does all the logic.
    @Autowired
    DocumentService documentService;
    @Autowired
    UserService userService;

    @RequestMapping(value = "getAll", method = RequestMethod.GET)
    public ResponseEntity<Response> getAll(@RequestParam(required = false) Long parentFolderId, @RequestAttribute Long userId) throws AccountNotFoundException {
        return facadeController.getAll(parentFolderId, userId);
    }

    @RequestMapping(value = "folder/rename", method = RequestMethod.PATCH)
    public ResponseEntity<Response> renameFolder(@RequestParam Long folderId, @RequestParam String name, @RequestAttribute Long userId) {
        return facadeController.rename(folderId, name, Folder.class);
    }

    @RequestMapping(value = "document/rename", method = RequestMethod.PATCH)
    public ResponseEntity<Response> renameDocument(@RequestParam Long documentId, @RequestParam String name, @RequestAttribute Long userId) {
        return facadeController.rename(documentId, name, Document.class);
    }

    @RequestMapping(value = "folder", method = RequestMethod.DELETE)
    public ResponseEntity<Response> deleteFolder(@RequestParam Long folderId, @RequestAttribute Long userId) {
        return facadeController.delete(folderId, Folder.class);
    }

    @RequestMapping(value = "document", method = RequestMethod.DELETE)
    public ResponseEntity<Response> deleteDocument(@RequestParam Long documentId, @RequestAttribute Long userId) {
        return facadeController.delete(documentId, Document.class);
    }

    @RequestMapping(value = "folder/relocate", method = RequestMethod.PATCH)
    public ResponseEntity<Response> relocateFolder(@RequestParam Long newParentFolderId, @RequestParam Long folderId, @RequestAttribute Long userId) {
        return facadeController.relocate(newParentFolderId, folderId, Folder.class);

    }

    @RequestMapping(value = "document/relocate", method = RequestMethod.PATCH)
    public ResponseEntity<Response> relocateDocument(@RequestParam Long newParentFolderId, @RequestParam Long documentId, @RequestAttribute Long userId) {
        return facadeController.relocate(newParentFolderId, documentId, Document.class);
    }

    @RequestMapping(value = "document/export", method = RequestMethod.GET)
    public ResponseEntity<Response> export(@RequestParam Long documentId, @RequestAttribute Long userId) {
        return facadeController.export(documentId);
    }


    @RequestMapping(value = "document/doesExists", method = RequestMethod.GET)
    public ResponseEntity<Response> doesDocumentExists(@RequestParam Long documentId, @RequestAttribute Long userId) {
        return facadeController.doesExist(documentId, Document.class);
    }

    @RequestMapping(value = "folder/doesExists", method = RequestMethod.GET)
    public ResponseEntity<Response> doesFolderExists(@RequestParam Long folderId, @RequestAttribute Long userId) {
        return facadeController.doesExist(folderId, Folder.class);
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

            // CONSULT: I think we should return this response as an object everytime.
            //  either if it's an OK status or BAD status.
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
            return facadeController.create(doc, Document.class);

        } catch (FileNotFoundException | AccountNotFoundException e) {
            return new ResponseEntity<>(new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .message(e.getMessage())
                    .build(), HttpStatus.BAD_REQUEST);
        }
    }

    //FIXME: this function has been split into two functions: getDocumentPath and getFolderPath. this should be removed once the client is updated.
    @RequestMapping(value = "getPath", method = RequestMethod.GET)
    public ResponseEntity<Response> getPath(@RequestParam Type type, @RequestParam Long fileId, @RequestAttribute Long userId) {
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

            if (type.equals(Type.FOLDER)) {
                path.add(0, new FileRes(generalItem.getName(), generalItem.getId(), Type.FOLDER, Permission.ADMIN, generalItem.getUser().getEmail()));
            }
            while (parentFolder != null) {
                path.add(0, new FileRes(parentFolder.getName(), parentFolder.getId(), Type.FOLDER, Permission.ADMIN, generalItem.getUser().getEmail()));
                parentFolder = parentFolder.getParentFolder();
            }
            return new ResponseEntity<>(new Response.Builder()
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .message("Successfully managed to retrieve the requested path")
                    .data(path)
                    .build(), HttpStatus.OK);

        } catch (FileNotFoundException e) {
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
            return facadeController.getPath(document, Document.class);
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
            return facadeController.getPath(folder, Folder.class);
        } catch (FileNotFoundException e) {
            return new ResponseEntity<>(new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(400)
                    .message(e.getMessage())
                    .build(), HttpStatus.BAD_REQUEST);
        }
    }

    // FIXME: this function has been relocated to userController, so this function will be removed once the client is updated
    @RequestMapping(value = "document/getUser", method = RequestMethod.GET)
    public ResponseEntity<?> getUser(@RequestParam Long documentId, @RequestAttribute Long userId) {
        try {
            User user = userService.findById(userId);
            Permission permission = documentService.getUserPermissionInDocument(userId, documentId);
            return new ResponseEntity<>(new Response.Builder()
                    .status(HttpStatus.OK)
                    .statusCode(200)
                    .data(new JoinRes(user.getName(), userId, permission))
                    .message("Successfully managed to get the user from the database")
                    .build(), HttpStatus.OK);

        } catch (AccountNotFoundException e) {
            return new ResponseEntity<>(new Response.Builder()
                    .message(e.getMessage())
                    .statusCode(400)
                    .status(HttpStatus.BAD_REQUEST)
                    .build(), HttpStatus.BAD_REQUEST);
        }
    }
}