package docSharing.controller;

import docSharing.entity.Log;
import docSharing.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class TextEditController {

    @Autowired
    DocumentService documentService;

    @MessageMapping("/document")
    @SendTo("/document")
    public Log receiveLog(@Payload Log log) {
        if (log.getData() == null || log.getAction() == null || log.getOffset() == null || log.getDocumentId() == null || log.getUserId() == null || log.getCreationDate() == null)
            // FIXME: What to do if anything fails? Do we do anything with the client?
            return null;

        documentService.updateContent(log);
        return log;
    }

    @MessageMapping("/document/getContent")
    @SendTo("/document/getContent")
    public String getContent(@Payload Log log) {
        return documentService.getContent(log.getDocumentId());
    }
}
