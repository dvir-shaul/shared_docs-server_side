package docSharing.controller;

import docSharing.entity.Log;
import docSharing.entity.User;
import docSharing.requests.OnlineUsersReq;
import docSharing.service.DocumentService;
import docSharing.service.UserService;
import docSharing.utils.ConfirmationToken;
import docSharing.utils.Validations;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;

@Controller
@CrossOrigin
public class TextEditController {

    @Autowired
    DocumentService documentService;
    @Autowired
    UserService userService;

    @MessageMapping("/document/{documentId}")
    @SendTo("/document/{documentId}")
    public Log receiveLog(@DestinationVariable Long documentId, @Payload Log log) {
//        if (log.getData() == null || log.getAction() == null || log.getOffset() == null || log.getDocumentId() == null || log.getUserId() == null || log.getCreationDate() == null)
//            // FIXME: What to do if anything fails? Do we do anything with the client?
//            return null;
        log.setUserId(Validations.validateToken("Bearer " + log.getToken()));
        Log copyOfLog = Log.copy(log);
        documentService.updateContent(log);
        System.out.println("Creating a new log for: " + copyOfLog);
        return copyOfLog;
    }

    @MessageMapping("/document/getContent/{documentId}")
    @SendTo("/document/getContent/{documentId}")
    public String getContent(@DestinationVariable Long documentId, @Payload Log log) {
        System.out.println("Getting all content for the document: " + log.getDocumentId());
        String content = documentService.getContent(log.getDocumentId());
        return content;
    }

    @MessageMapping("/document/onlineUsers/{documentId}")
    @SendTo("/document/onlineUsers/{documentId}")
    public List<String> getOnlineUsers(@DestinationVariable Long documentId, @Payload OnlineUsersReq onlineUsersReq) {
        System.out.println("Looking for online users for document id:" + onlineUsersReq.getDocumentId());
        Long userId = Validations.validateToken("Bearer " + onlineUsersReq.getToken());
        Set<User> onlineUsers = documentService.updateActiveUsersOfDoc(userId, onlineUsersReq.getDocumentId(), onlineUsersReq.getMethod());
        return onlineUsers.stream().map(u -> u.getName()).collect(Collectors.toList());
    }

    @MessageMapping("/document/join/{documentId}")
    @SendTo("/document/join/{documentId}")
    public Long join(@DestinationVariable Long documentId, @Payload String token) {
        System.out.println("Getting user id for token: " + token);
        Claims claims = ConfirmationToken.decodeJWT(token);
        return Long.valueOf(claims.getId());
    }

}
