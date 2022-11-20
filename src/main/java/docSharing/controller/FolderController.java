package docSharing.controller;

import docSharing.entity.Document;
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
@RequestMapping(value = "/file")
@AllArgsConstructor
class FileController {

    @RequestMapping(value = "folder", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> create(@RequestBody Folder folder, @RequestHeader(value = "token") String token) {
        return AbstractController.validateAndRoute(folder, token, Action.CREATE);
    }

    @RequestMapping(value = "document", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> create(@RequestBody Document doc, @RequestHeader(value = "token") String token) {
        return AbstractController.validateAndRoute(doc, token, Action.CREATE);
    }

    @RequestMapping(value = "folder/rename", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> rename(@RequestBody Folder folder, @RequestHeader(value = "token") String token) {
        return AbstractController.validateAndRoute(folder, token, Action.RENAME);
    }

    @RequestMapping(value = "document/rename", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> rename(@RequestBody Document doc, @RequestHeader(value = "token") String token) {
        return AbstractController.validateAndRoute(doc, token, Action.RENAME);
    }

    @RequestMapping(value = "folder", method = RequestMethod.DELETE, consumes = "application/json")
    public ResponseEntity<?> delete(@RequestBody Folder folder, @RequestHeader(value = "token") String token) {
        return AbstractController.validateAndRoute(folder, token, Action.DELETE);
    }

    @RequestMapping(value = "document", method = RequestMethod.DELETE, consumes = "application/json")
    public ResponseEntity<?> delete(@RequestBody Document doc, @RequestHeader(value = "token") String token) {
        return AbstractController.validateAndRoute(doc, token, Action.DELETE);
    }

    @RequestMapping(value = "folder/relocate", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> relocate(@RequestBody Folder folder, @RequestHeader(value = "token") String token) {
        return AbstractController.validateAndRoute(folder, token, Action.RELOCATE);
    }

    @RequestMapping(value = "document/relocate", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> relocate(@RequestBody Document doc, @RequestHeader(value = "token") String token) {
        return AbstractController.validateAndRoute(doc, token, Action.RELOCATE);
    }
}