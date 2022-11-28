package docSharing.filter;

import com.google.api.client.json.Json;
import com.google.gson.Gson;
import docSharing.entity.*;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserDocumentRepository;
import docSharing.repository.UserRepository;
import docSharing.utils.ExceptionMessage;
import docSharing.utils.Validations;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.server.ResponseStatusException;

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

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("----------PermissionFilter-----------");
        boolean flag = false;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        // Permission//
        String token = httpRequest.getHeader("authorization");
        List<String> list = List.of(httpRequest.getRequestURI().split("/"));
        if (list.contains("auth")) {
            // we are in auth controller
            flag= true;
        }
        Long Id = getId(httpRequest);
        Long userId = Validations.validateToken(token);
        System.out.println(userId);
        System.out.println(Id);

        if (!flag && list.contains("folder")) {
            Optional<Folder> optFolder = folderRepository.findById(Id);
            if (!optFolder.isPresent())
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
            Folder folder = optFolder.get();

            if (httpRequest.getMethod().equals("PATCH") || httpRequest.getMethod().equals("DELETE")) {
                if (folder.getUser().getId().equals(userId)) {
                    System.out.println("work");
                    flag= true;
                } else {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED_USER.toString());
                }
            }
            if (httpRequest.getMethod().equals("POST") || httpRequest.getMethod().equals("GET")) {
                flag= true;
            }
            chain.doFilter(request, response);
            return;
        }

        if (!flag && list.contains("document")) {
            if (list.contains("file")) {
                if (httpRequest.getMethod().equals("POST")) {
                    flag= true;
                }
            }
            UserDocument userDocument = getUserDocument(Id, userId);

            if (!flag && list.contains("file")) {
                if (httpRequest.getMethod().equals("PATCH")) {
                    if (userDocument.getPermission().equals(Permission.MODERATOR)) {
                        flag= true;;
                    }
                }
                if (httpRequest.getMethod().equals("DELETE") && !(userDocument.getPermission().equals(Permission.MODERATOR))) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED_USER.toString());
                }
            }
            if (!flag && httpRequest.getMethod().equals("POST")) {
                // text edit controller
                if (userDocument.getPermission().equals(Permission.VIEWER)) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED_USER.toString());
                }
                flag= true;
            }
        }


        if(!flag && list.contains("permission")){//in permissions
            UserDocument userDocument = getUserDocument(Id, userId);
            System.out.println("................................");
            if(userDocument.getPermission().equals(Permission.MODERATOR)){
                System.out.println("................................");
                chain.doFilter(request, response);
                flag= true;
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED_USER.toString());
        }

    }

    public static String getBody(ServletRequest request) throws IOException {
        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }

        body = stringBuilder.toString();
        return body;
    }


    public Long getId(ServletRequest request) throws IOException {
        Long docId = null;
        String payloadRequest = getBody(request);
        List<String> list1 = List.of(payloadRequest.split(","));
        for (String s : list1) {
            if (s.contains("id")) {
                String tempId = s.split(":")[1].split("}")[0].trim();
                docId = Long.parseLong(tempId);
            }
        }
        return docId;
    }

    public UserDocument getUserDocument(Long docId, Long userId) throws IOException {
        Optional<Document> optDocument = documentRepository.findById(docId);
        Optional<User> optUser = userRepository.findById(userId);
        if (!optUser.isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExceptionMessage.NO_USER_IN_DATABASE.toString());
        if (!optDocument.isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString());
        Document document = optDocument.get();
        User user = optUser.get();
        Optional<UserDocument> optUserDocument = userDocumentRepository.find(document, user);
        if (!optUserDocument.isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExceptionMessage.NO_USER_IN_DOCUMENT_IN_DATABASE.toString());
        return optUserDocument.get();
    }
}


/**
 * user1
 * eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxIiwiaWF0IjoxNjY5NjI5Mzk5LCJzdWIiOiJsb2dpbiIsImlzcyI6ImRvY3MgYXBwIn0.4Qcz2o6NzXVzJXLl0IdJec6-vCRGnzBLM11H2bmsaPQ
 * user2
 * eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIyIiwiaWF0IjoxNjY5NjM4MzYyLCJzdWIiOiJsb2dpbiIsImlzcyI6ImRvY3MgYXBwIn0.QQtT3liScCSUqleIBVbTNw232MNExjK4b196i9w09ak
 * user2
 * eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIyIiwiaWF0IjoxNjY5NjM4MzYyLCJzdWIiOiJsb2dpbiIsImlzcyI6ImRvY3MgYXBwIn0.QQtT3liScCSUqleIBVbTNw232MNExjK4b196i9w09ak
 */
/** user2
 *eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIyIiwiaWF0IjoxNjY5NjM4MzYyLCJzdWIiOiJsb2dpbiIsImlzcyI6ImRvY3MgYXBwIn0.QQtT3liScCSUqleIBVbTNw232MNExjK4b196i9w09ak
 */
