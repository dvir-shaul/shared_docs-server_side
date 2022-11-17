package docSharing.controller;

import docSharing.entity.Document;
import docSharing.service.AuthService;
import docSharing.service.DocumentService;
import docSharing.utils.ExceptionMessage;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/document")
@AllArgsConstructor
public class DocumentController implements ChangesController {

    @Autowired
    DocumentService documentService;
    @Autowired
    AuthService authService;

    @RequestMapping(value = "create", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<String> create(@RequestBody Document doc, @RequestHeader(value = "token") String token) {
        Long folderId = doc.getFolderId();
        String title = doc.getTitle();

        // make sure we got all the data from the client
        if (title == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You must include all and exact parameters for such an action: title");
        //CONSULT: if a folderId is null, does it mean it has to be in the root folder?
//        if (folderId == null)
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not proceed with this action without passing containing folder id");

        try {
            // validate token returns userId
            Long userId = authService.validateToken(token);
            // send it to create document.
            return ResponseEntity.ok().body(documentService.create(userId, title, folderId).toString());
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ExceptionMessage.UNAUTHORIZED.toString());
        }
    }
}
