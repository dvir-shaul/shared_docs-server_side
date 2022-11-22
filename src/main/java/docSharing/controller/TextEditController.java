package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Log;
import docSharing.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import javax.print.Doc;
import java.util.Date;
import java.util.Map;

@Controller
public class TextEditController {

    @Autowired
    DocumentService documentService;


    @MessageMapping("/document")
    @SendTo("/document")
    public ResponseEntity<?> receiveLog(@Payload Log log) {
        if (log.getData() == null || log.getAction() == null || log.getOffset() == null ||
                log.getDocumentId()==null || log.getUserId()==null || log.getCreationDate()==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such action allowed");
        }

        documentService.updateContent(log);

        // parse token to id
//        Document doc = documentService.getDocById(log.getDocumentId());
//        documentService.updateContent(doc.getContent() + log.getData(), doc.getId());
//        return documentService.getDocById(log.getDocumentId());
        return ResponseEntity.ok().body(log.getData());
    }

    @MessageMapping("/document/getContent")
    @SendTo("/document/getContent")
    public String getContent(@Payload Log log) {
        return documentService.getContent(log.getDocumentId());
    }

}
