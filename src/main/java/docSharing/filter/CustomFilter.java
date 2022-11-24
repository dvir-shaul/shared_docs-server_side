package docSharing.filter;

import docSharing.utils.Validations;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class CustomFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

//        String url = ((HttpServletRequest) request).getRequestURL().toString();
//        System.out.println(url);
//        Boolean isAuth = url.contains("auth");
//        System.out.println(isAuth);
//
//        // FIXME: improve the filtering of auth functions -> login and register.
//        if (!isAuth) {
//            // Authorization
//            String token = ((HttpServletRequest) request).getHeader("Authorization");
        // Long userId = authService.authUser(token)
//            // TODO: add userId to the request
//
//            // Permission
//
//        }

        chain.doFilter(request, response);
    }
}