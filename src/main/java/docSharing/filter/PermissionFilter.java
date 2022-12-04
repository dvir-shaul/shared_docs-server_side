package docSharing.filter;

import docSharing.entity.*;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserDocumentRepository;
import docSharing.repository.UserRepository;
import docSharing.service.AuthService;
import docSharing.service.EmailService;
import docSharing.utils.ExceptionMessage;
import docSharing.utils.Params;
import docSharing.utils.Validations;

import docSharing.utils.grantPermission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.server.ResponseStatusException;

import javax.security.auth.login.AccountNotFoundException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@Component
public class PermissionFilter extends GenericFilterBean {

    private static Logger logger = LogManager.getLogger(PermissionFilter.class.getName());
    @Autowired
    AuthService authService;
    @Autowired
    UserDocumentRepository userDocumentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    FolderRepository folderRepository;

    /**
     * this doFilter function is set to check if the user has the permission to do the action he
     * wanted, according to his role that saved in the userDocumentRepository, this repo has the information
     * about all document and all the users that watch each document and his role.
     *
     * @param request  - request from client
     * @param response - response if the action can be done or not.
     * @param chain    - chain of filters to go through
     * @throws IOException      -
     * @throws ServletException -
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        logger.info("in PermissionFilter -> doFilter");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        List<String> list = List.of(httpRequest.getRequestURI().split("/"));

        boolean flag = false;

        if (actionThatDontRequirePermission(request, list)) {
            flag = true;
        }
        try {
            if (!flag && list.contains("folder")) {
                if (actionOnFolder(request)) {
                    flag = true;
                }
            }

            if (!flag && list.contains("document")) {
                if (actionOnDocument(request, list)) {
                    flag = true;
                }
            }

            if (!flag && list.contains("permission")) {
                if (actionIsChangePermission(request)) {
                    flag = true;
                }

            }
        }catch (ResponseStatusException | AccountNotFoundException e){
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED,"Not Authorized");
            throw new IllegalAccessError("Not Authorized");
        }
        if (!flag) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED,ExceptionMessage.WRONG_SEARCH.toString());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExceptionMessage.WRONG_SEARCH.toString());
        }

        chain.doFilter(request, response);
    }


    /**
     * actionThatDontRequirePermission checks if the given request from the client doesn't need to check for permission,
     * i.e:  get path of a file doesn't need permission, so it bypass our filter.
     *
     * @param uriList - list of URI request - httpRequest.getRequestURI().split("/").
     * @return - if the action need to bypass the filter
     */
    public boolean actionThatDontRequirePermission(ServletRequest request, List<String> uriList) {
        logger.info("in PermissionFilter -> actionThatDontRequirePermission");

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        for (String s : grantPermission.list) {
            if (uriList.contains(s))
                return true;
        }
        if (httpRequest.getMethod().equals(HttpMethod.OPTIONS.toString()) ||
                httpRequest.getMethod().equals(HttpMethod.GET.toString())) {
            return true;
        }
        return false;
    }

    /**
     * filter checks when we get a permission change request from client,
     * checks if we can grant access to the user's request.
     *
     * @return - if the given request can go through the filter.
     */
    public boolean actionIsChangePermission(ServletRequest request) throws ResponseStatusException, AccountNotFoundException {
        logger.info("in PermissionFilter -> actionIsChangePermission");

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = httpRequest.getHeader("authorization");
        Long docId = Long.valueOf(request.getParameter(Params.DOCUMENT_ID.toString()));
        Long userId = authService.isValid(token);
        UserDocument userDocument = getUserDocument(docId, userId);
        if (userDocument.getPermission().equals(Permission.MODERATOR) || userDocument.getDocument().getUser().getId().equals(userId)) {
            return true;
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED_USER.toString());
        }
    }

    /**
     * filter checks when we get a document change request from client,
     *
     * @param list - list - list of URI request - httpRequest.getRequestURI().split("/").
     * @return - if the given request can go through the filter.
     */
    public boolean actionOnDocument(ServletRequest request, List<String> list) throws ResponseStatusException {
        logger.info("in PermissionFilter -> actionOnDocument");

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = httpRequest.getHeader("authorization");

        if (list.contains("file")) {
            if (httpRequest.getMethod().equals(HttpMethod.POST.toString())) {// create new document
                return true;
            }
        }

        if (request.getParameter(Params.DOCUMENT_ID.toString()) != null) { // checks if we got any parameters
            Long docId = Long.valueOf(request.getParameter(Params.DOCUMENT_ID.toString()));
            Long userId = Validations.validateToken(token);
            UserDocument userDocument = getUserDocument(docId, userId);

            if (list.contains("file")) {
                if (httpRequest.getMethod().equals(HttpMethod.PATCH.toString())) {// relocate / rename
                    if (userDocument.getPermission().equals(Permission.MODERATOR)) {
                        return true;
                    }else{
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED_USER.toString());
                    }
                }
                if (httpRequest.getMethod().equals(HttpMethod.DELETE.toString()) && !(userDocument.getDocument().getUser().getId().equals(userId))) {// only the creator can delete a file
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED_USER.toString());
                }
                return true;
            }
            // FIXME: What to do on text edit with logs/getContent/onlineUsers?
//            if (httpRequest.getMethod().equals(HttpMethod.POST.toString())) {// text edit controller
//                if (userDocument.getPermission().equals(Permission.VIEWER)) {//viewer can't add logs on a document
//                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED_USER.toString());
//                }
//                return true;
//            }
        }
        return false;
    }

    /**
     * filter checks when we get a folder change request from client,
     *
     * @return - if the given request can go through the filter.
     */
    public boolean actionOnFolder(ServletRequest request) {
        logger.info("in PermissionFilter -> actionOnFolder");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = httpRequest.getHeader("authorization");

        if (httpRequest.getMethod().equals(HttpMethod.PATCH.toString()) || httpRequest.getMethod().equals(HttpMethod.DELETE.toString())) {
            Long FolderId = Long.valueOf(request.getParameter(Params.FOLDER_ID.toString()));
            Long userId = Validations.validateToken(token);
            Optional<Folder> optFolder = folderRepository.findById(FolderId);
            if (!optFolder.isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
            }
            Folder folder = optFolder.get();
            if (folder.getUser().getId().equals(userId)) {
                return true;
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED_USER.toString());
            }
        }
        if (httpRequest.getMethod().equals(HttpMethod.POST.toString()) ) {
            return true;
        }
        return false;
    }

    /**
     * @return - entity of UserDocument.
     */
    public UserDocument getUserDocument(Long docId, Long userId) throws ResponseStatusException {
        logger.info("in PermissionFilter -> getUserDocument");
        Document document = getDocument(docId);
        User user = getUser(userId);
        Optional<UserDocument> optUserDocument = userDocumentRepository.find(document, user);
        if (!optUserDocument.isPresent()) {
            logger.error("in PermissionFilter -> getUserDocument ->" + ExceptionMessage.NO_USER_IN_DOCUMENT_IN_DATABASE);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExceptionMessage.NO_USER_IN_DOCUMENT_IN_DATABASE.toString());
        }
        return optUserDocument.get();
    }

    /**
     * @return - entity of Document.
     */
    public Document getDocument(Long docId) throws ResponseStatusException {
        logger.info("in PermissionFilter -> getDocument");

        Optional<Document> optDocument = documentRepository.findById(docId);
        if (!optDocument.isPresent()) {
            logger.error("in PermissionFilter -> getDocument ->" + ExceptionMessage.NO_DOCUMENT_IN_DATABASE);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString());
        }
        return optDocument.get();
    }

    /**
     * @return - entity of User.
     */
    public User getUser(Long userId) throws ResponseStatusException {
        logger.info("in PermissionFilter -> getUser");

        Optional<User> optUser = userRepository.findById(userId);
        if (!optUser.isPresent()) {
            logger.error("in PermissionFilter -> getUser ->" + ExceptionMessage.NO_USER_IN_DATABASE);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExceptionMessage.NO_USER_IN_DATABASE.toString());
        }
        return optUser.get();
    }
}
