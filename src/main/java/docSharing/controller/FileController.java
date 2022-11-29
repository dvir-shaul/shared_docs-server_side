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
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
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
    public ResponseEntity<List<FileRes>> getAll(@RequestParam(required = false) Long parentFolderId, @RequestAttribute Long userId) {
        System.out.println("userId: " + userId);
            return ac.getAll(parentFolderId, userId);
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
        Document doc = Document.createDocument(user, docReq.getName(), parentFolder, docReq.getContent());
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



    @RequestMapping(value = "document/export", method = RequestMethod.GET)
    public ResponseEntity<?> export(@RequestParam Long documentId, @RequestAttribute Long userId) {
        Document document = documentService.findById(documentId).get();
        ExportDoc exportDoc = new ExportDoc(document.getName(), document.getContent());
        return ResponseEntity.ok().body(exportDoc);
    }

    @RequestMapping(value = "getPath", method = RequestMethod.GET)
    public ResponseEntity<?> getPath(@RequestParam Type type, @RequestParam Long fileId, @RequestAttribute Long userId) {
        List<FileRes> path = new ArrayList<>();
        GeneralItem generalItem = null;
        switch (type) {
            case FOLDER:
                generalItem = folderService.findById(fileId).get();
                break;
            case DOCUMENT:
                generalItem = documentService.findById(fileId).get();
                break;
        }
        Folder parentFolder = generalItem.getParentFolder();
            path.add(0, new FileRes(generalItem.getName(), generalItem.getId(), type));
        while (parentFolder != null) {
            path.add(0, new FileRes(parentFolder.getName(), parentFolder.getId(), Type.FOLDER));
            parentFolder = parentFolder.getParentFolder();
        }
        return ResponseEntity.ok(path);
    }

    @RequestMapping(value = "document/isExists", method = RequestMethod.GET)
    public ResponseEntity<?> documentExists(@RequestParam Long documentId, @RequestAttribute Long userId) {
        return ResponseEntity.ok(documentService.findById(documentId).isPresent());
    }
    @RequestMapping(value = "document/getUser", method = RequestMethod.POST)
    public ResponseEntity<?> getUser(@RequestParam Long documentId,  @RequestAttribute Long userId) {
        Permission permission = documentService.getUserPermissionInDocument(userId, documentId);
        return ResponseEntity.ok(new JoinRes(userId, permission));
    }
    @RequestMapping("/document/getContent/{documentId}")
    public String getContent(@DestinationVariable Long documentId, @RequestAttribute Long userId) {
        String content = documentService.getContent(documentId);
        return content;
    }
}