package docSharing.controller;

import docSharing.entity.Folder;
import docSharing.utils.Action;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/folder")
@AllArgsConstructor
class FolderController extends AbstractController {

    @RequestMapping(value = "create", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> create(@RequestBody Folder folder, @RequestHeader(value = "token") String token) {
        return validateAndRoute(folder, token, Action.CREATE);
    }

    @RequestMapping(value = "rename", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> rename(@RequestBody Folder folder, @RequestHeader(value = "token") String token) {
        return validateAndRoute(folder, token, Action.RENAME);
    }

    @RequestMapping(value = "delete", method = RequestMethod.DELETE, consumes = "application/json")
    public ResponseEntity<?> delete(@RequestBody Folder folder, @RequestHeader(value = "token") String token) {
        return validateAndRoute(folder, token, Action.DELETE);
    }

    /**
     * @param folder - Asks for the following params to be sent in order for it to work: folderId*, parentFolderId.
     *               "*" means this field is required.
     * @param token
     * @return
     */
    @RequestMapping(value = "relocate", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> relocate(@RequestBody Folder folder, @RequestHeader(value = "token") String token) {
        return validateAndRoute(folder, token, Action.RELOCATE);
    }
}