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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/file")
@AllArgsConstructor
@NoArgsConstructor
class FileController {

    @Autowired
    AbstractController ac;
    @Autowired
    FolderService folderService;

    @RequestMapping(value = "folder", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> create(@RequestBody CreateFolderReq folderReq, @RequestHeader(value = "Authorization") String token) {
        Folder parentFolder = folderService.findById(folderReq.getParentFolderId()).get();
        Folder folder = Folder.createFolder(folderReq.getName(), parentFolder);
        return ac.validateAndRoute(folder, token, Action.CREATE);
    }

    @RequestMapping(value = "document", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> create(@RequestBody CreateDocumentReq docReq, @RequestHeader(value = "Authorization") String token) {
        Folder parentFolder=folderService.findById(docReq.getParentFolderId()).get();
        Document doc=Document.createDocument(docReq.getName(), parentFolder);
        return ac.validateAndRoute(doc, token, Action.CREATE);
    }

    @RequestMapping(value = "folder/rename", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> rename(@RequestBody Folder folder, @RequestHeader(value = "Authorization") String token) {
        return ac.validateAndRoute(folder, token, Action.RENAME);
    }

    @RequestMapping(value = "document/rename", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> rename(@RequestBody Document doc, @RequestHeader(value = "Authorization") String token) {
        return ac.validateAndRoute(doc, token, Action.RENAME);
    }

    @RequestMapping(value = "folder", method = RequestMethod.DELETE, consumes = "application/json")
    public ResponseEntity<?> delete(@RequestBody Folder folder, @RequestHeader(value = "Authorization") String token) {
        return ac.validateAndRoute(folder, token, Action.DELETE);
    }

    @RequestMapping(value = "document", method = RequestMethod.DELETE, consumes = "application/json")
    public ResponseEntity<?> delete(@RequestBody Document doc, @RequestHeader(value = "Authorization") String token) {
        return ac.validateAndRoute(doc, token, Action.DELETE);
    }

    @RequestMapping(value = "folder/relocate", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> relocate(@RequestBody RelocateFolderReq folderReq, @RequestHeader(value = "Authorization") String token) {
        Folder parentFolder = folderService.findById(folderReq.getNewParentId()).get();
        Folder folder = folderService.findById(folderReq.getId()).get();
        folder.setParentFolder(parentFolder);
        return ac.validateAndRoute(folder, token, Action.RELOCATE);
    }

    @RequestMapping(value = "document/relocate", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> relocate(@RequestBody CreateDocumentReq docReq, @RequestHeader(value = "Authorization") String token) {
        Folder parentFolder=folderService.findById(docReq.getParentFolderId()).get();
        Document doc=Document.createDocument(docReq.getName(), parentFolder);
        return ac.validateAndRoute(doc, token, Action.RELOCATE);
    }
}