package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.GeneralItem;
import docSharing.entity.Folder;
import docSharing.entity.User;
import docSharing.service.*;
import docSharing.utils.Action;
import docSharing.utils.ExceptionMessage;
import docSharing.utils.Regex;
import docSharing.utils.Validations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AbstractController {

    @Autowired
    DocumentService documentService;
    @Autowired
    FolderService folderService;
    @Autowired
    UserService userService;

    public ResponseEntity<?> validateAndRoute(GeneralItem item, String token, Action action) {
        Long userId;
        try {
            userId = AuthService.validateToken(token);
            User user=userService.findById(userId).get();
            item.setUser(user);
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

    private ResponseEntity<String> create(GeneralItem item) {
        // make sure we got all the data from the client
        try {
            Validations.validate(Regex.FILE_NAME.getRegex(), item.getName());
//            Validations.validate(Regex.ID.getRegex(), item.getParentFolderId().toString());
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return ResponseEntity.ok().body(convertFromItemToService(item).create(item).toString());
    }

    private ResponseEntity<Object> rename(GeneralItem item) {
        String name = item.getName();
        Long folderId = item.getId();

        if (name == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You must include all and exact parameters for such an action: name");

        return ResponseEntity.ok().body(String.valueOf(convertFromItemToService(item).rename(folderId, name)));
    }

    private ResponseEntity<String> delete(GeneralItem item) {
        Long id = item.getId();

        if (id == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("I'm sorry. In order for me to delete a document, you need to be more specific about its id... So what is its id?");

        convertFromItemToService(item).delete(id);
        return ResponseEntity.ok().body("A document answering to the id:" + id + " has been successfully erased from the database!");
    }

    private ResponseEntity<Object> relocate(GeneralItem item) {
        Folder parentFolder = item.getParentFolder();
        Long folderId = item.getId();

        if (folderId == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);

        return ResponseEntity.ok().body(convertFromItemToService(item).relocate(parentFolder, folderId));
    }

    /**
     * This function gets an item as a parameter and extracts its class in order to return the correct service.
     *
     * @param item
     * @return
     */
    private ServiceInterface convertFromItemToService(GeneralItem item) {
        if (item instanceof Document) return documentService;
        if (item instanceof Folder) return folderService;
        return null;
    }
}