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
import docSharing.utils.ConfirmationToken;
import docSharing.utils.Validations;
import io.jsonwebtoken.Claims;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static Logger logger = LogManager.getLogger(TextEditController.class.getName());

    @Autowired
    DocumentService documentService;
    @Autowired
    UserService userService;
    @Autowired
    LogService logService;

    /**
     * receiveLog is a function that's called from the client when we have changes in a specific document id,
     * it contains the logReq with new data to update the document content.
     *
     * @param documentId - document id in database.
     * @param logReq     - log request with: userId, documentId, offset, data, action.
     * @return -LogReq from the client.
     */
    @MessageMapping("/document/{documentId}")
    @SendTo("/document/{documentId}")
    public LogReq receiveLog(@DestinationVariable Long documentId, @Payload LogReq logReq) {
        logger.info("in TextEditController -> receiveLog");
        try {
            User user = userService.findById(logReq.getUserId());
            Document document = documentService.findById(documentId);
            Log log = new Log(user, document, logReq.getOffset(), logReq.getData(), logReq.getAction(), LocalDateTime.now());
            documentService.updateContent(log);
            logService.updateLogs(log);
            return logReq;
        } catch (AccountNotFoundException e) {
            logger.fatal("in TextEditController -> receiveLog ->" + e.getMessage());
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            logger.fatal("in TextEditController -> receiveLog ->" + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    /**
     * getOnlineUsers called with a document id and user with method add or remove,
     * goal is to keep an update list of users that use a document.
     *
     * @param documentId     - document id in database.
     * @param onlineUsersReq - new request with user and method ADD, REMOVE the user from the document.
     * @return - list of users that use the given document's id.
     */
    @MessageMapping("/document/onlineUsers/{documentId}")
    @SendTo("/document/onlineUsers/{documentId}")
    public List<UsersInDocRes> getOnlineUsers(@DestinationVariable Long documentId, @Payload OnlineUsersReq onlineUsersReq) {
        logger.info("in TextEditController -> getOnlineUsers");
        try {
            List<UsersInDocRes> all = documentService.getAllUsersInDocument(onlineUsersReq.getUserId(), documentId, onlineUsersReq.getMethod());
            Collections.sort(all, new Comparator<UsersInDocRes>() {
                public int compare(UsersInDocRes o1, UsersInDocRes o2) {
                    return o1.compareTo(o2);
                }
            });
            return all;
        } catch (IllegalArgumentException | AccountNotFoundException e) {
            logger.debug("in TextEditController -> getOnlineUsers -> no users to get online");
            return null;
        }
    }
}
