package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Folder;
import docSharing.entity.GeneralItem;
import docSharing.service.AuthService;
import docSharing.service.DocumentService;
import docSharing.service.FolderService;
import docSharing.service.ServiceInterface;
import docSharing.utils.Action;
import docSharing.utils.ExceptionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class AbstractController {

    @Autowired
    AuthService authService;
    @Autowired
    DocumentService documentService;
    @Autowired
    FolderService folderService;

    public ResponseEntity<?> validateAndRoute(GeneralItem item, String token, Action action) {

        Long userId;
        try {
//            Long userId = authService.validateToken(token);
            userId = item.getUserId();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ExceptionMessage.UNAUTHORIZED.toString());
        }

        switch (action) {
            case CREATE:
                return create(item, userId);
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

    private ResponseEntity<String> create(GeneralItem item, Long userId) {

        Long folderId = item.getParentFolderId();
        String name = item.getName();

        // make sure we got all the data from the client
        if (name == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You must include all and exact parameters for such an action: name");

        // TODO: create validation for document name: make sure it doesn't have special characters!

        // CONSULT: if a folderId is null, does it mean it has to be in the root folder?
//        if (folderId == null)
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not proceed with this action without passing containing folder id");

        if(userId != item.getUserId())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ExceptionMessage.UNAUTHORIZED.toString());

        // send it to create document.
        return ResponseEntity.ok().body(convertFromItemToService(item).create(userId, name, folderId).toString());
    }

    private ResponseEntity<Object> rename(GeneralItem item) {
        String name = item.getName();
        Long folderId = item.getId();

        if (name == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You must include all and exact parameters for such an action: name");

        return ResponseEntity.ok().body(String.valueOf(convertFromItemToService(item).rename(folderId, name)));
    }

    private ResponseEntity<String> delete(GeneralItem item) {
        Long folderId = item.getId();

        if (folderId == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("I'm sorry. In order for me to delete a document, you need to be more specific about its id... So what is its id?");

        convertFromItemToService(item).delete(folderId);
        return ResponseEntity.ok().body("A document answering to the id:" + folderId + " has been successfully erased from the database!");
    }

    private ResponseEntity<Object> relocate(GeneralItem item) {
        Long parentFolderId = item.getParentFolderId();
        Long folderId = item.getId();

        if (folderId == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);

        return ResponseEntity.ok().body(convertFromItemToService(item).relocate(parentFolderId, folderId));
    }

    /**
     * This function gets an item as a parameter and extracts its class in order to return the correct service.
     * @param item
     * @return
     */
    private ServiceInterface convertFromItemToService(GeneralItem item) {
        if (item instanceof Document) return documentService;
        if (item instanceof Folder) return folderService;
        return null;
    }
}