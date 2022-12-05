package docSharing.controller;

import docSharing.entity.Permission;
import docSharing.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {
    private static Logger logger = LogManager.getLogger(UserController.class.getName());

    @Autowired
    private FacadeUserController facadeUserController;


    /**
     * givePermission is a PATCH function for changing the user role.
     * @param documentId - the document id in database
     * @param uid - the user id in database that will change his permission
     * @param permission - the new permission
     * @param userId - the user id that sent this request
     * @return ResponseEntity with the message, if it worked or not.
     */
    @RequestMapping(value = "/permission/give", method = RequestMethod.PATCH)
    public ResponseEntity<Response> givePermission(@RequestParam Long documentId, @RequestParam Long uid, @RequestParam Permission permission, @RequestAttribute Long userId) {
        logger.info("in UserController -> givePermission");
        Response response = facadeUserController.givePermission(documentId, uid, permission);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * givePermissionToAll is a POST function for sharing a document with given a list of emails.
     * if the user's email is not in our database it will send him an invitation to register the app.
     * @param emails - list with emails to share the document.
     * @param documentId - document id in database.
     * @param userId - the user that sends this request
     * @return ResponseEntity with a message.
     */

    @RequestMapping(value = "/share", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Response> givePermissionToAll(@RequestBody List<String> emails, @RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in UserController -> givePermissionToAll");
        Response response = facadeUserController.givePermissionToAll(emails, documentId);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * getDocuments is a GET method that sends all the document the user have linked with him.
     * @param userId - id in database.
     * @return - list with document.
     */
    @RequestMapping(value = "sharedDocuments", method = RequestMethod.GET)
    public ResponseEntity<Response> getDocuments(@RequestAttribute Long userId) {
        logger.info("in UserController -> getDocuments");
        Response response = facadeUserController.getDocuments(userId);
        return new ResponseEntity<>(response, response.getStatus());
    }
    /**
     * getUser is a GET method that sends an entity of user to the client.
     * @param userId - user's id in database.
     * @return - entity of UserRes that's contain name,email and id.
     */
    @RequestMapping(value = "getUser", method = RequestMethod.GET)
    public ResponseEntity<Response> getUser(@RequestAttribute Long userId) {
        logger.info("in UserController -> getUser");
        Response response = facadeUserController.getUser(userId);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     *
     * @param documentId
     * @param userId
     * @return
     */
    @RequestMapping(value = "document/getUser", method = RequestMethod.GET)
    public ResponseEntity<Response> getUserPermissionForSpecificDocument(@RequestParam Long documentId, @RequestAttribute Long userId) {
        logger.info("in UserController -> getUserPermissionForSpecificDocument");
        Response response = facadeUserController.getUserPermissionForSpecificDocument(documentId, userId);
        return new ResponseEntity<>(response, response.getStatus());
    }
}
