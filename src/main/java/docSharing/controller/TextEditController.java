package docSharing.controller;

import docSharing.entity.Log;
import docSharing.entity.User;
import docSharing.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

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
        Log copyOfLog = Log.copy(log);
        documentService.updateContent(log);
        return copyOfLog;
    }

    @MessageMapping("/document/getContent")
    @SendTo("/document/getContent")
    public String getContent(@Payload Log log) {
        return documentService.getContent(log.getDocumentId());
    }

    @MessageMapping("/document/onlineUsers")
    @SendTo("/document/onlineUsers")
    public List<User> getOnlineUsers() {
        return new ArrayList<>();
    }
}
