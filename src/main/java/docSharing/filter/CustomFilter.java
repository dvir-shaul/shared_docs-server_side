package docSharing.filter;

import docSharing.utils.Validations;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;


public class CustomFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("in filter");
        String url = ((HttpServletRequest) request).getRequestURL().toString();
        String method = ((HttpServletRequest) request).getMethod();
        if( method.equals(RequestMethod.OPTIONS.toString())){
            Enumeration<String> headers=((HttpServletRequest) request).getHeaderNames();
            System.out.println(headers);
        }
        if (!url.contains("auth") && !url.contains("ws"))
        // Authorization
        {
            String auth = ((HttpServletRequest) request).getHeader("Authorization");
            Long userId = Validations.validateToken(auth);
            request.setAttribute("userId", userId);
        }

        // Permission
        chain.doFilter(request, response);
    }
}