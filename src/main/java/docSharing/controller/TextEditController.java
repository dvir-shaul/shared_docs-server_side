package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Log;
import docSharing.entity.User;
import docSharing.requests.OnlineUsersReq;
import docSharing.service.DocumentService;
import docSharing.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;

import javax.websocket.server.ServerEndpoint;

@Controller
@CrossOrigin
public class TextEditController {

    @Autowired
    DocumentService documentService;
    @Autowired
    UserService userService;

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
    public List<String> getOnlineUsers(@Payload OnlineUsersReq onlineUsersReq) {
        Document document = documentService.getDocById(onlineUsersReq.getDocId());
        User user = userService.findById(onlineUsersReq.getUserId()).get();
        document.addOnlineUser(user);
        Set<User> onlineUsers= documentService.getOnlineUsers(document);
        return onlineUsers.stream().map(u->u.getName()).collect(Collectors.toList());
    }
    @MessageMapping("/document/removeUser")
    @SendTo("/document/removeUser")
    public List<String> removeUser(@Payload OnlineUsersReq onlineUsersReq) {
        Document document = documentService.getDocById(onlineUsersReq.getDocId());
        User user = userService.findById(onlineUsersReq.getUserId()).get();
        document.removeOnlineUser(user);
        Set<User> onlineUsers= documentService.getOnlineUsers(document);
        return onlineUsers.stream().map(u->u.getName()).collect(Collectors.toList());
    }
}
