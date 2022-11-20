package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.File;
import docSharing.entity.Folder;
import docSharing.service.AuthService;
import docSharing.service.DocumentService;
import docSharing.service.FolderService;
import docSharing.service.ServiceInterface;
import docSharing.utils.Action;
import docSharing.utils.ExceptionMessage;
import docSharing.utils.Regex;
import docSharing.utils.Validations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.naming.AuthenticationException;

public class AbstractController {

    @Autowired
    static AuthService authService;
    @Autowired
    static DocumentService documentService;
    @Autowired
    static FolderService folderService;

    public static ResponseEntity<?> validateAndRoute(File item, String token, Action action) {
        Long userId;
        try {
            userId = authService.validateToken(token);
            item.setId(userId);
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ExceptionMessage.UNAUTHORIZED.toString());
        }

        switch (action) {
            case CREATE:
                return create(item);
            case DELETE:
                return delete(item);
            case RELOCATE:
                return relocate(item);
            case RENAME:
                return rename(item);
            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such action allowed");
        }
    }

    private static ResponseEntity<String> create(File item) {
        // make sure we got all the data from the client
        try {
            Validations.validate(Regex.FILE_NAME.getRegex(), item.getName());
            Validations.validate(Regex.ID.getRegex(), item.getParentFolderId().toString());
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return ResponseEntity.ok().body(convertFromItemToService(item).create(item).toString());
    }

    private static ResponseEntity<Object> rename(File item) {
        String name = item.getName();
        Long folderId = item.getId();

        if (name == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You must include all and exact parameters for such an action: name");

        return ResponseEntity.ok().body(String.valueOf(convertFromItemToService(item).rename(folderId, name)));
    }

    private static ResponseEntity<String> delete(File item) {
        Long id = item.getId();

        if (id == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("I'm sorry. In order for me to delete a document, you need to be more specific about its id... So what is its id?");

        convertFromItemToService(item).delete(id);
        return ResponseEntity.ok().body("A document answering to the id:" + id + " has been successfully erased from the database!");
    }

    private static ResponseEntity<Object> relocate(File item) {
        Long parentFolderId = item.getParentFolderId();
        Long folderId = item.getId();

        if (folderId == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);

        return ResponseEntity.ok().body(convertFromItemToService(item).relocate(parentFolderId, folderId));
    }

    /**
     * This function gets an item as a parameter and extracts its class in order to return the correct service.
     *
     * @param item
     * @return
     */
    private static ServiceInterface convertFromItemToService(File item) {
        if (item instanceof Document) return documentService;
        if (item instanceof Folder) return folderService;
        return null;
    }
}