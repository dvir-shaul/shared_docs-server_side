package docSharing.filter;

import com.google.api.client.json.Json;
import com.google.gson.Gson;
import docSharing.entity.*;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserDocumentRepository;
import docSharing.repository.UserRepository;
import docSharing.utils.ExceptionMessage;
import docSharing.utils.Params;
import docSharing.utils.Validations;
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PermissionFilter extends GenericFilterBean {


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
     * @param request - request from client
     * @param response - response if the action can be done or not.
     * @param chain - chain of filters to go through
     * @throws IOException -
     * @throws ServletException -
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        boolean flag = false;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = httpRequest.getHeader("authorization");
        List<String> list = List.of(httpRequest.getRequestURI().split("/"));
        if (list.contains("auth") ||list.contains("getAll")  ) {
            flag = true;
        }

        if (!flag && list.contains("folder")) {
            if (httpRequest.getMethod().equals(HttpMethod.PATCH.toString()) || httpRequest.getMethod().equals(HttpMethod.DELETE.toString())) {
                Long FolderId = Long.valueOf(request.getParameter(Params.FOLDER_ID.toString()));
                Long userId = Validations.validateToken(token);
                Optional<Folder> optFolder = folderRepository.findById(FolderId);
                if (!optFolder.isPresent())
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
                Folder folder = optFolder.get();
                if (folder.getUser().getId().equals(userId)) {
                    flag = true;
                } else {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED_USER.toString());
                }
            }
            //TODO: CHECK AGAIN
            if (httpRequest.getMethod().equals(HttpMethod.POST.toString()) || httpRequest.getMethod().equals(HttpMethod.GET.toString())) {
//                if (request.getParameter(Params.PARENT_FOLDER_ID.toString()) != null) {
//                    Long parentFolderId = Long.valueOf(request.getParameter(Params.PARENT_FOLDER_ID.toString()));
//                    Optional<Folder> optFolder = folderRepository.findById(parentFolderId);
//                    if (!optFolder.isPresent())
//                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
//                    Folder folder = optFolder.get();
//                }
                flag=true;
            }
        }
//
        if (!flag && list.contains("document")) {
            if (list.contains("getPath") || list.contains("import") || list.contains("export")) { // no need of permissions
                flag = true;
            }
            if (list.contains("file")) {
                if (httpRequest.getMethod().equals(HttpMethod.POST.toString())) {// create new document
                    flag = true;
                }
            }
            if (request.getParameter(Params.DOCUMENT_ID.toString()) != null) { // checks if we got any parameters
                Long docId = Long.valueOf(request.getParameter(Params.DOCUMENT_ID.toString()));
                Long userId = Validations.validateToken(token);
                UserDocument userDocument = getUserDocument(docId, userId);

                if (!flag && list.contains("file")) {
                    if (httpRequest.getMethod().equals(HttpMethod.PATCH.toString())) {// relocate / rename
                        if (userDocument.getPermission().equals(Permission.MODERATOR)) {
                            flag = true;
                        }
                    }
                    if (httpRequest.getMethod().equals(HttpMethod.DELETE.toString()) && !(userDocument.getDocument().getUser().getId().equals(userId))) {// only the creator can delete a file
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED_USER.toString());
                    }
                }
                // FIXME: What to do on text edit with logs/getContent/onlineUsers?
                if (!flag && httpRequest.getMethod().equals(HttpMethod.POST.toString())) {// text edit controller
                    if (userDocument.getPermission().equals(Permission.VIEWER)) {//viewer can't add logs on a document
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED_USER.toString());
                    }
                    flag = true;
                }
            }
        }

        if (!flag && list.contains("permission")) {//in permissions
            Long docId = Long.valueOf(request.getParameter(Params.DOCUMENT_ID.toString()));
            Long userId = Validations.validateToken(token);
            UserDocument userDocument = getUserDocument(docId, userId);
            if (userDocument.getPermission().equals(Permission.MODERATOR) || userDocument.getDocument().getUser().getId().equals(userId)) {
                flag = true;
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED_USER.toString());
            }
        }
        if(! flag) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExceptionMessage.WRONG_SEARCH.toString());
        chain.doFilter(request, response);
    }

    public UserDocument getUserDocument(Long docId, Long userId) throws IOException {
        Document document = getDocument(docId);
        User user = getUser(userId);
        Optional<UserDocument> optUserDocument = userDocumentRepository.find(document, user);
        if (!optUserDocument.isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExceptionMessage.NO_USER_IN_DOCUMENT_IN_DATABASE.toString());
        return optUserDocument.get();
    }


    public Document getDocument(Long docId) throws IOException {
        Optional<Document> optDocument = documentRepository.findById(docId);
        if (!optDocument.isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString());
        return optDocument.get();
    }

    public User getUser(Long userId) throws IOException {
        Optional<User> optUser = userRepository.findById(userId);
        if (!optUser.isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExceptionMessage.NO_USER_IN_DATABASE.toString());
        return optUser.get();
    }
}


/**
 * user1
 * eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxIiwiaWF0IjoxNjY5NzQxMDkxLCJzdWIiOiJsb2dpbiIsImlzcyI6ImRvY3MgYXBwIn0.Uz6NzXGJLu62GHhFBQC36GNB5cAhXCVMGrnzUyzlBVo
 * user2
 * eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIyIiwiaWF0IjoxNjY5NjM4MzYyLCJzdWIiOiJsb2dpbiIsImlzcyI6ImRvY3MgYXBwIn0.QQtT3liScCSUqleIBVbTNw232MNExjK4b196i9w09ak
 */
//public static String getBody(ServletRequest request) throws IOException {
//        System.out.println("getbody");
//        StringBuilder stringBuilder = new StringBuilder();
//        BufferedReader bufferedReader = null;
////        String body = request.getReader().lines()
////                .reduce("", (accumulator, actual) -> accumulator + actual);
//        String test = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
////        try {
////            InputStream inputStream = request.getInputStream();
////            if (inputStream != null) {
////                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
////                char[] charBuffer = new char[128];
////                int bytesRead = -1;
////                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
////                    stringBuilder.append(charBuffer, 0, bytesRead);
////                }
////            } else {
////                stringBuilder.append("");
////            }
////        } finally {
////            if (bufferedReader != null) {
////                bufferedReader.close();
////            }
////        }
//
////        body = stringBuilder.toString();
//        System.out.println(test);
//        return test;
//    }
//
//
//    public Long getId(ServletRequest request) throws IOException {
//        Long id = null;
//        String payloadRequest = getBody(request);
//        List<String> list1 = List.of(payloadRequest.split(","));
//        for (String s : list1) {
//            if (s.contains("Id") || s.contains("id")) {
//                String tempId = s.split(":")[1].split("}")[0].trim();
//                id = Long.parseLong(tempId);
//            }
//        }
//        System.out.println(id);
//        return id;
//    }
