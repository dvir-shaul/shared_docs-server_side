package docSharing.controller;

import docSharing.entity.Document;
import docSharing.utils.Action;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/document")
@AllArgsConstructor
public class DocumentController extends AbstractController {

    @RequestMapping(value = "create", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> create(@RequestBody Document doc, @RequestHeader(value = "token") String token) {
        return validateAndRoute(doc, token, Action.CREATE);
    }

    @RequestMapping(value = "rename", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> rename(@RequestBody Document doc, @RequestHeader(value = "token") String token){
        return validateAndRoute(doc, token, Action.RENAME);
    }

    @RequestMapping(value = "delete", method = RequestMethod.DELETE, consumes = "application/json")
    public ResponseEntity<?> delete(@RequestBody Document doc, @RequestHeader(value = "token") String token){
        return validateAndRoute(doc, token, Action.DELETE);
    }


    @RequestMapping(value = "relocate", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<?> relocate(@RequestBody Document doc, @RequestHeader(value = "token") String token){
        return validateAndRoute(doc, token, Action.RELOCATE);
    }

}