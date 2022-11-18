package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Folder;
import docSharing.entity.GeneralItem;
import docSharing.service.AuthService;
import docSharing.service.DocumentService;
import docSharing.service.FolderService;
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
            userId = Long.valueOf(1);
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
        return ResponseEntity.ok().body(useCreateFunctionInServiceByItem(item, userId, name, folderId).toString());
    }

    private ResponseEntity<Object> rename(GeneralItem item) {
        String name = item.getName();
        Long folderId = item.getId();

        if (name == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You must include all and exact parameters for such an action: name");

        return ResponseEntity.ok().body(String.valueOf(folderService.rename(folderId, name)));
    }

    private ResponseEntity<Boolean> delete(GeneralItem item) {
        Long folderId = item.getId();

        if (folderId == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        folderService.delete(folderId);
        return ResponseEntity.ok().body(true);
    }

    private ResponseEntity<Object> relocate(GeneralItem item) {
        Long parentFolderId = item.getParentFolderId();
        Long folderId = item.getId();

        if (folderId == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);

        return ResponseEntity.ok().body(folderService.relocate(parentFolderId, folderId));
    }

    private Long useCreateFunctionInServiceByItem(GeneralItem item, Long userId, String name, Long folderId) {
        if (item instanceof Document) return documentService.create(userId, name, folderId);
        if (item instanceof Folder) return folderService.create(userId, name, folderId);
        return null;
    }
}