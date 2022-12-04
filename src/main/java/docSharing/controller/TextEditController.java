package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.Log;
import docSharing.entity.User;
import docSharing.requests.LogReq;
import docSharing.requests.OnlineUsersReq;
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

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    public LogReq receiveLog(@DestinationVariable Long documentId, @Payload LogReq logReq) {
//            // FIXME: What to do if anything fails? Do we do anything with the client?
        try {
            // FIXME: what if there's no such a user? Do we handle it?
            User user = userService.findById(logReq.getUserId());
            // FIXME: what if there's no such a document? Do we check it?
            Document document = documentService.findById(documentId);
            // CONSULT: Why do we even get a logReq and not a normal Log. Then return a logRes?
            Log log = new Log(user, document, logReq.getOffset(), logReq.getData(), logReq.getAction(), LocalDateTime.now());
            documentService.updateContent(log);
            logService.updateLogs(log);
            return logReq;
        } catch (AccountNotFoundException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    @MessageMapping("/document/onlineUsers/{documentId}")
    @SendTo("/document/onlineUsers/{documentId}")
    public List<UsersInDocRes> getOnlineUsers(@DestinationVariable Long documentId, @Payload OnlineUsersReq onlineUsersReq) {
        try {
            List<UsersInDocRes> all = documentService.getAllUsersInDocument(documentId);
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
