package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Folder;
import docSharing.entity.User;
import docSharing.requests.CreateDocumentReq;
import docSharing.requests.CreateFolderReq;
import docSharing.requests.RelocateDocReq;
import docSharing.requests.RelocateFolderReq;
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

    @RequestMapping(value = "/document",method = RequestMethod.OPTIONS)
    public ResponseEntity handle() {
        System.out.println("in options");
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "folder", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> create(@RequestBody CreateFolderReq folderReq, @RequestAttribute Long userId) {
        Folder parentFolder = null;
        if (folderReq.getParentFolderId() != null) {
            parentFolder = folderService.findById(folderReq.getParentFolderId()).get();
        }
        User user = userService.findById(userId).get();
        Folder folder = Folder.createFolder(folderReq.getName(), parentFolder, user);
        return ac.create(folder);
    }

    @RequestMapping(value = "document", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> create(@RequestBody CreateDocumentReq docReq, @RequestAttribute Long userId) {
        Folder parentFolder = null;
        if (docReq.getParentFolderId() != null) {
            parentFolder = folderService.findById(docReq.getParentFolderId()).get();
        }
        User user = userService.findById(userId).get();
        Document doc = Document.createDocument(user, docReq.getName(), parentFolder);
        return ac.create(doc);
    }

    @RequestMapping(value = "folder/rename", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> rename(@RequestBody Folder folder, @RequestAttribute Long userId) {
        return ac.rename(folder);
    }

    @RequestMapping(value = "document/rename", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> rename(@RequestBody Document doc, @RequestAttribute Long userId) {
        return ac.rename(doc);
    }

    @RequestMapping(value = "folder", method = RequestMethod.DELETE, consumes = "application/json")
    public ResponseEntity<?> delete(@RequestBody Folder folder, @RequestAttribute Long userId) {
        return ac.delete(folder);
    }

    @RequestMapping(value = "document", method = RequestMethod.DELETE, consumes = "application/json")
    public ResponseEntity<?> delete(@RequestBody Document doc, @RequestAttribute Long userId) {
        return ac.delete(doc);
    }

    @RequestMapping(value = "folder/relocate", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> relocate(@RequestBody RelocateFolderReq folderReq, @RequestAttribute Long userId) {
        Folder folder = folderService.findById(folderReq.getId()).get();
        return ac.relocate(folderReq.getNewParentId(), folder);
    }

    @RequestMapping(value = "document/relocate", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> relocate(@RequestBody RelocateDocReq docReq, @RequestAttribute Long userId) {
        Document doc = documentService.findById(docReq.getId()).get();
        return ac.relocate(docReq.getNewParentId(), doc);
    }
}