package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Log;
import docSharing.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import javax.print.Doc;
import java.util.Map;

@Controller
public class TextEditController {

    @Autowired
    DocumentService documentService;

    @MessageMapping("/document")
    @SendTo("/document")
    public Log receiveLog(@Payload Log log) {
        System.out.println(log);
        if (log.getData() == null || log.getAction() == null || log.getOffset() == null) return null;
        documentService.updateContent(log);
        return log;
    }

    @MessageMapping("/document/getContent")
    @SendTo("/document/getContent")
    public String getContent(@Payload Log log) {
        System.out.println(log);
        return documentService.getContent(log.getDocumentId());
    }
}
