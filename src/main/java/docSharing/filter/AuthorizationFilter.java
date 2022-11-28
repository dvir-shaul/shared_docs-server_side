package docSharing.filter;

import com.google.gson.Gson;
import docSharing.entity.Permission;
import docSharing.entity.User;
import docSharing.repository.UserDocumentRepository;
import docSharing.repository.UserRepository;
import docSharing.utils.Validations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.stream.Collectors;
@Component
public class AuthorizationFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        String url = ((HttpServletRequest) request).getRequestURL().toString();
        System.out.println(url);
        HttpServletRequest httpRequest = (HttpServletRequest) request;

//         Authorization
        if (!url.contains("auth") && !url.contains("ws")) {
            if ((httpRequest.getHeader("access-control-request-headers") != null)) {
                if (!httpRequest.getHeader("access-control-request-headers").equals("authorization"))
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "A request must include authorization field");
            } else if (httpRequest.getHeader("authorization") != null) {
                String token = httpRequest.getHeader("authorization");
                if (token == null)
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Could not find a token in the request");
//                System.out.println("token: " + token);
                Long userId = Validations.validateToken(token);
//                System.out.println("userId: " + userId);
                request.setAttribute("userId", userId);
            }
        }
        System.out.println("----------AuthorizationFilter-----------");
        chain.doFilter(request, response);
    }
}
