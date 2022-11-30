package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.GeneralItem;
import docSharing.entity.Folder;
import docSharing.entity.Permission;
import docSharing.requests.Type;
import docSharing.response.FileRes;
import docSharing.service.*;
import docSharing.utils.Regex;
import docSharing.utils.Validations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.security.auth.login.AccountNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Component
public class AbstractController {

    @Autowired
    DocumentService documentService;
    @Autowired
    FolderService folderService;

    public ResponseEntity<List<FileRes>> getAll(Long parentFolderId, Long userId) {
        List<Folder> folders;
        List<Document> documents;
        if (parentFolderId != null) {
            try {
                folders = folderService.get(parentFolderId, userId);
                documents = documentService.get(parentFolderId, userId);
            } catch (AccountNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                folders = folderService.getAllWhereParentFolderIsNull(userId);
                documents = documentService.getAllWhereParentFolderIsNull(userId);

            } catch (AccountNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseEntity.ok().body(convertToFileRes(folders, documents));
    }

    private List<FileRes> convertToFileRes(List<Folder> folders, List<Document> documents) {
        List<FileRes> fileResList = new ArrayList<>();
        for (Folder folder :
                folders) {

            fileResList.add(new FileRes(folder.getName(), folder.getId(), Type.FOLDER, Permission.ADMIN, folder.getUser().getEmail()));
        }
        for (Document document :
                documents) {
            fileResList.add(new FileRes(document.getName(), document.getId(), Type.DOCUMENT, Permission.ADMIN, document.getUser().getEmail()));
        }
        return fileResList;
    }


    public ResponseEntity<String> create(GeneralItem item, Class c) {
        // make sure we got all the data from the client
        try {
            Validations.validate(Regex.FILE_NAME.getRegex(), item.getName());
//            Validations.validate(Regex.ID.getRegex(), item.getParentFolderId().toString());
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return ResponseEntity.ok().body(convertFromClassToService(c).create(item).toString());
    }

    public ResponseEntity<Object> rename(Long id, String name, Class c) {
        if (name == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You must include all and exact parameters for such an action: name");

        return ResponseEntity.ok().body(String.valueOf(convertFromClassToService(c).rename(id, name)));
    }

    public ResponseEntity<String> delete(Long id, Class c) {
        if (id == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("I'm sorry. In order for me to delete a document, you need to be more specific about its id... So what is its id?");
        convertFromClassToService(c).delete(id);
        return ResponseEntity.ok().body("An item answering to the id:" + id + " has been successfully erased from the database!");
    }

    public ResponseEntity<Object> relocate(Long newParentId, Long id, Class c) {
        Folder parentFolder = null;
        try {
            if (newParentId != null) {
                parentFolder = folderService.findById(newParentId);
            }
            if (id == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);

            return ResponseEntity.ok().body(convertFromClassToService(c).relocate(parentFolder, id));
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
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
}