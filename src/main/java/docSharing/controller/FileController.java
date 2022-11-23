package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Folder;
import docSharing.repository.FolderRepository;
import docSharing.requests.CreateDocumentReq;
import docSharing.requests.CreateFolderReq;
import docSharing.requests.RelocateFolderReq;
import docSharing.service.FolderService;
import docSharing.utils.Action;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @RequestMapping(value = "document", method = RequestMethod.GET)
    public ResponseEntity<?> getAllDocuments() {
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "folder", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> create(@RequestBody CreateFolderReq folderReq, @RequestHeader(value = "Authorization") String token) {
        Folder parentFolder = folderService.findById(folderReq.getParentFolderId()).get();
        Folder folder = Folder.createFolder(folderReq.getName(), parentFolder);
        return ac.create(folder);
    }

    @RequestMapping(value = "document", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> create(@RequestBody CreateDocumentReq docReq, @RequestHeader(value = "Authorization") String token) {
        Folder parentFolder=folderService.findById(docReq.getParentFolderId()).get();
        Document doc=Document.createDocument(docReq.getName(), parentFolder);
        return ac.create(doc);
    }

    @RequestMapping(value = "folder/rename", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> rename(@RequestBody Folder folder) {
        return ac.rename(folder);
    }

    @RequestMapping(value = "document/rename", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> rename(@RequestBody Document doc) {
        return ac.rename(doc);
    }

    @RequestMapping(value = "folder", method = RequestMethod.DELETE, consumes = "application/json")
    public ResponseEntity<?> delete(@RequestBody Folder folder) {
        return ac.delete(folder);
    }

    @RequestMapping(value = "document", method = RequestMethod.DELETE, consumes = "application/json")
    public ResponseEntity<?> delete(@RequestBody Document doc) {
        return ac.delete(doc);
    }

    @RequestMapping(value = "folder/relocate", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> relocate(@RequestBody RelocateFolderReq folderReq, @RequestHeader(value = "Authorization") String token) {
        Folder parentFolder = folderService.findById(folderReq.getNewParentId()).get();
        Folder folder = folderService.findById(folderReq.getId()).get();
        folder.setParentFolder(parentFolder);
        return ac.relocate(folder);
    }

    @RequestMapping(value = "document/relocate", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> relocate(@RequestBody CreateDocumentReq docReq, @RequestHeader(value = "Authorization") String token) {
        Folder parentFolder=folderService.findById(docReq.getParentFolderId()).get();
        Document doc=Document.createDocument(docReq.getName(), parentFolder);
        return ac.relocate(doc);
    }
}