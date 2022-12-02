package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Log;
import docSharing.entity.User;
import docSharing.requests.LogReq;
import docSharing.requests.OnlineUsersReq;
import docSharing.response.AllUsers;
import docSharing.response.UserStatus;
import docSharing.response.UsersInDocRes;
import docSharing.service.DocumentService;
import docSharing.service.LogService;
import docSharing.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;

import javax.security.auth.login.AccountNotFoundException;

@Controller
@CrossOrigin
public class TextEditController {

    @Autowired
    DocumentService documentService;
    @Autowired
    UserService userService;
    @Autowired
    LogService logService;

    @MessageMapping("/document/{documentId}")
    @SendTo("/document/{documentId}")
//    public Log receiveLog(@DestinationVariable Long documentId, @Payload Log log) {
    public LogReq receiveLog(@DestinationVariable Long documentId, @Payload LogReq logReq) {
//        if (log.getData() == null || log.getAction() == null || log.getOffset() == null || log.getDocumentId() == null || log.getUserId() == null || log.getCreationDate() == null)
//            // FIXME: What to do if anything fails? Do we do anything with the client?
//            return null;
        try {
            // FIXME: what if there's no such a user? Do we handle it?
            User user = userService.findById(logReq.getUserId());
            // FIXME: what if there's no such a document? Do we check it?
            Document document = documentService.findById(documentId);
            Log log = new Log(user, document, logReq.getOffset(), logReq.getData(), logReq.getAction(), LocalDateTime.now());
            LogReq copyOfLog = new LogReq(log.getUser().getId(), log.getDocument().getId(), log.getOffset(), log.getData(), log.getAction());
            documentService.updateContent(log);
            logService.updateLogs(log);

            return copyOfLog;
        } catch (AccountNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //return one map with status

    @MessageMapping("/document/onlineUsers/{documentId}")
    @SendTo("/document/onlineUsers/{documentId}")
    public List<UsersInDocRes> getOnlineUsers(@DestinationVariable Long documentId, @Payload OnlineUsersReq onlineUsersReq) {
        try {
            System.out.println("Looking for online users for document id:" + documentId);
            Set<Long> onlineUsers = documentService.addUserToDocActiveUsers(onlineUsersReq.getUserId(), documentId, onlineUsersReq.getMethod()).stream().map(u -> u.getId()).collect(Collectors.toSet());
            List<UsersInDocRes> all = documentService.getAllUsersInDocument(documentId).stream().map(u -> new UsersInDocRes(u.getUser().getId(), u.getUser().getName(), u.getUser().getEmail(), u.getPermission(), onlineUsers.contains(u.getUser().getId()) ? UserStatus.ONLINE : UserStatus.OFFLINE)).collect(Collectors.toList());
            Collections.sort(all, new Comparator<UsersInDocRes>() {
                public int compare(UsersInDocRes o1, UsersInDocRes o2) {
                    return o1.compareTo(o2);
                }
            });
            return all;
        } catch (AccountNotFoundException e) {
            return null;
        }
    }


}
