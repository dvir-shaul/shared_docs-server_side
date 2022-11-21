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

@Controller
public class TextEditController {

    @Autowired
    DocumentService documentService;

    @MessageMapping("/document")
    @SendTo("/document")
    public Document receiveLog(@Payload Log log){
        Document doc = documentService.getDocById(log.getDocumentId());
        documentService.updateContent(doc.getContent() + log.getData(), doc.getId());
        return documentService.getDocById(log.getDocumentId());
    }

//    @MessageMapping("/private-message")
//    public Log recMessage(@Payload Log log){
//        simpMessagingTemplate.convertAndSendToUser(message.getReceiverName(),"/private",message);
//        System.out.println(message.toString());
//        return message;
//    }

}
