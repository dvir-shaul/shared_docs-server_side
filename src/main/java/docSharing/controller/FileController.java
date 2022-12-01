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
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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

    @Autowired
    AbstractController ac;
    @Autowired
    FolderService folderService;
    @Autowired
    DocumentService documentService;
    @Autowired
    UserService userService;


    @RequestMapping(value = "getAll", method = RequestMethod.GET)
    public ResponseEntity<List<FileRes>> getAll(@RequestParam(required = false) Long parentFolderId, @RequestAttribute Long userId) throws AccountNotFoundException {
        return ac.getAll(parentFolderId, userId);
    }

    @RequestMapping(value = "folder", method = RequestMethod.POST)
    public ResponseEntity<?> createFolder(@RequestParam(required = false) Long parentFolderId, @RequestParam String name, @RequestAttribute Long userId) {
        Folder parentFolder = null;
        try {
            if (parentFolderId != null) {
                parentFolder = folderService.findById(parentFolderId);
            }
            User user = userService.findById(userId);
            Folder folder = Folder.createFolder(name, parentFolder, user);
            return ac.create(folder, Folder.class);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    @RequestMapping(value = "document", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> createDocument(@RequestParam Long parentFolderId, @RequestParam String name, @RequestBody(required = false) String content, @RequestAttribute Long userId) {
        try {
            Folder parentFolder = folderService.findById(parentFolderId);
            User user = userService.findById(userId);
            Document doc = Document.createDocument(user, name, parentFolder, content != null ? content : "");
            return ac.create(doc, Document.class);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @RequestMapping(value = "folder/rename", method = RequestMethod.PATCH)
    public ResponseEntity<?> renameFolder(@RequestParam Long folderId, @RequestParam String name, @RequestAttribute Long userId) {
        if(name==null || name.length()==0 ){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionMessage.VALIDATION_FAILED+"name is empty/null");
        }
        return ac.rename(folderId, name, Folder.class);
    }

    @RequestMapping(value = "document/rename", method = RequestMethod.PATCH)
    public ResponseEntity<?> renameDocument(@RequestParam Long documentId, @RequestParam String name, @RequestAttribute Long userId) {
        if(name==null || name.length()==0 ){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionMessage.VALIDATION_FAILED+"name is empty/null");
        }
        return ac.rename(documentId, name, Document.class);
    }

    @RequestMapping(value = "folder", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteFolder(@RequestParam Long folderId, @RequestAttribute Long userId) {
        return ac.delete(folderId, Folder.class);
    }

    @RequestMapping(value = "document", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteDocument(@RequestParam Long documentId, @RequestAttribute Long userId) {
        return ac.delete(documentId, Document.class);
    }

    @RequestMapping(value = "folder/relocate", method = RequestMethod.PATCH)
    public ResponseEntity<?> relocateFolder(@RequestParam Long newParentFolderId, @RequestParam Long folderId, @RequestAttribute Long userId) {
        try {
            Folder folder = folderService.findById(folderId);
            return ac.relocate(newParentFolderId, folderId, Folder.class);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @RequestMapping(value = "document/relocate", method = RequestMethod.PATCH)
    public ResponseEntity<?> relocateDocument(@RequestParam Long newParentFolderId, @RequestParam Long documentId, @RequestAttribute Long userId) {
        try {
            Document doc = documentService.findById(documentId);
            return ac.relocate(newParentFolderId, documentId, Document.class);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @RequestMapping(value = "document/export", method = RequestMethod.GET)
    public ResponseEntity<?> export(@RequestParam Long documentId, @RequestAttribute Long userId) {
        try {
            Document document = documentService.findById(documentId);
            ExportDoc exportDoc = new ExportDoc(document.getName(), document.getContent());
            return ResponseEntity.ok().body(exportDoc);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @RequestMapping(value = "getPath", method = RequestMethod.GET)
    public ResponseEntity<?> getPath(@RequestParam Type type, @RequestParam Long fileId, @RequestAttribute Long userId) {
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @RequestMapping(value = "document/isExists", method = RequestMethod.GET)
    public ResponseEntity<?> documentExists(@RequestParam Long documentId, @RequestAttribute Long userId) {
        try {
            return ResponseEntity.ok(documentService.findById(documentId));
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @RequestMapping(value = "document/getUser", method = RequestMethod.POST)
    public ResponseEntity<?> getUser(@RequestParam Long documentId, @RequestAttribute Long userId) {
        try {
            User user = userService.findById(userId);
            Permission permission = documentService.getUserPermissionInDocument(userId, documentId);
            return ResponseEntity.ok(new JoinRes(user.getName(), userId, permission));
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @RequestMapping(value = "/document/getContent", method = RequestMethod.GET)
    public ResponseEntity<String> getContent(@RequestParam Long documentId, @RequestAttribute Long userId) {
        String content = documentService.getContent(documentId);
        return ResponseEntity.ok().body(content);
    }
    @RequestMapping(value = "/document/name", method = RequestMethod.GET)
    public ResponseEntity<String> getDocumentName(@RequestParam Long documentId, @RequestAttribute Long userId) {
        try {
            Document document=documentService.findById(documentId);
            return ResponseEntity.ok(document.getName());
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}