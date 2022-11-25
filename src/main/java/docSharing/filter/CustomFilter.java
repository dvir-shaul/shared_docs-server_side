package docSharing.filter;

import docSharing.utils.Validations;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

public class CustomFilter extends GenericFilterBean {

    // FIXME: split Authorization and Permission to separated filters

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//        Enumeration<String> headerNames = httpRequest.getHeaderNames();
//
//        if (headerNames != null) {
//            while (headerNames.hasMoreElements()) {
//                Object x = headerNames.nextElement();
//                System.out.println(x);
//                System.out.println("Header: " + httpRequest.getHeader(x.toString()));
//            }
//        }

//        String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxIiwiaWF0IjoxNjY5MjgyMDE2LCJzdWIiOiJsb2dpbiIsImlzcyI6ImRvY3MgYXBwIn0.vu3Q7tYN-G4PxXKnMalFyw8Io8GCgsFbEiNhjBUxOXo";
//        System.out.println("token: " + token);
        String url = ((HttpServletRequest) request).getRequestURL().toString();

//         Authorization
        if (!url.contains("auth") && !url.contains("ws")) {
            String token = ((HttpServletRequest) request).getHeader("Authorization".toLowerCase());
//            System.out.println("token: " + token);
            Long userId = Validations.validateToken(token);
//            System.out.println("userId: " + userId);
            request.setAttribute("userId", userId);
        }

        // Permission

        chain.doFilter(request, response);
    }
}