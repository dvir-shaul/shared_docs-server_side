package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Folder;
import docSharing.entity.GeneralItem;
import docSharing.entity.User;
import docSharing.requests.*;
import docSharing.response.PathItem;
import docSharing.response.ExportDoc;
import docSharing.service.DocumentService;
import docSharing.service.FolderService;
import docSharing.service.UserService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity handle() {
        System.out.println("Do I get to OPTIONS ?");
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "getAll", method = RequestMethod.GET)
    public ResponseEntity<List<GeneralItem>> get(@RequestParam Long parentFolderId, @RequestAttribute Long userId) {
        System.out.println("userId: " + userId);
        return ac.get(parentFolderId, userId);
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

    @RequestMapping(value = "document/import", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> importDoc(@RequestBody ImportDocReq importDocReq, @RequestAttribute Long userId) {
        User user = userService.findById(userId).get();
        Folder parentFolder = null;
        if (importDocReq.getParentFolderId() != null)
            parentFolder = folderService.findById(importDocReq.getParentFolderId()).get();
        Document document = Document.createDocument(user, importDocReq.getName(), parentFolder);
        document.setContent(importDocReq.getContent());
        return ResponseEntity.ok().body(documentService.create(document));
    }

    @RequestMapping(value = "document/export", method = RequestMethod.GET)
    public ResponseEntity<?> export(@RequestParam Long documentId, @RequestAttribute Long userId) {
        Document document = documentService.findById(documentId).get();
        ExportDoc exportDoc = new ExportDoc(document.getName(), document.getContent());
        return ResponseEntity.ok().body(exportDoc);
    }

    @RequestMapping(value = "document/getPath", method = RequestMethod.GET)
    public ResponseEntity<?> getPath(@RequestParam Long documentId, @RequestAttribute Long userId) {
        Queue<PathItem> path = new LinkedList<>();
        Document document = documentService.findById(documentId).get();
        Folder parentFolder=document.getParentFolder();
        path.add(new PathItem(document.getId(), document.getName()));
        do {
            path.add(new PathItem(parentFolder.getId(), parentFolder.getName()));
            parentFolder=parentFolder.getParentFolder();
        } while (parentFolder != null);
        return ResponseEntity.ok(path);
    }
}