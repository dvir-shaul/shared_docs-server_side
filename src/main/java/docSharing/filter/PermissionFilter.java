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

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        // Permission//
        String token = httpRequest.getHeader("authorization");
        List<String> list = List.of(httpRequest.getRequestURI().split("/"));
        System.out.println(httpRequest.getRequestURI());
        if (list.contains("user")) {
            chain.doFilter(request, response);
            // we are in auth controller
            return;
        }
        Long userId = Validations.validateToken(token);
        for (String s : list) {
            System.out.println(s);
        }
        System.out.println("----------------------pre doc");
        if (list.contains("document")) {
            if (list.contains("file")) {
                if (httpRequest.getMethod().equals("POST")) {
                    chain.doFilter(request, response);
                    return;
                }
                Long docId = getId(request);
                System.out.println("--------docId "+docId);
                Optional<Document> optDocument = documentRepository.findById(docId);
                Optional<User> optUser = userRepository.findById(userId);
                if (!optUser.isPresent()) {
                    System.out.println("1");
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExceptionMessage.NO_USER_IN_DATABASE.toString());
                }if (!optDocument.isPresent()) {
                    System.out.println("2");
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString());
                }
                Document document = optDocument.get();
                User user = optUser.get();
                System.out.println(user);
                System.out.println(document);

                // file controller
                if (Objects.equals(document.getUser().getId(), user.getId())) {
                    chain.doFilter(request, response);
                    return;
                }
                UserDocument userDocument = getUserDocument(request,userId);

                if (httpRequest.getMethod().equals("PATCH") && userDocument.getPermission().equals(Permission.MODERATOR)) {
                    chain.doFilter(request, response);
                    return;
                }
                if (httpRequest.getMethod().equals("DELETE")) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED_USER.toString());
                }
            } else {
                UserDocument userDocument = getUserDocument(request,userId);
                // text edit controller
                if (userDocument.getPermission().equals(Permission.VIEWER)) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED_USER.toString());
                }
                chain.doFilter(request, response);
                return;
            }
        }
        if (list.contains("folder")) {
            if (httpRequest.getMethod().equals("PATCH")) {
                chain.doFilter(request, response);
                return;
            }
            if (httpRequest.getMethod().equals("POST")) {
                chain.doFilter(request, response);
                return;
            }
            if (httpRequest.getMethod().equals("DELETE")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED_USER.toString());
            }
            // we are in file controller
            chain.doFilter(request, response);
            return;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED_USER.toString());
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
                System.out.println("-------------------------------");
                String tempId = s.split(":")[1].split("}")[0].trim();
                System.out.println(tempId);
                docId = Long.parseLong(tempId);
            }
        }
        return docId;
    }

    public UserDocument getUserDocument(ServletRequest request, Long userId) throws IOException {
        Long docId = getId(request);
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
 * eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxIiwiaWF0IjoxNjY5NjI5Mzk5LCJzdWIiOiJsb2dpbiIsImlzcyI6ImRvY3MgYXBwIn0.4Qcz2o6NzXVzJXLl0IdJec6-vCRGnzBLM11H2bmsaPQ
 */

