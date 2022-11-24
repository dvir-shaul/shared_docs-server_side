package docSharing.filter;

import docSharing.utils.Validations;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class CustomFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Authorization
        String auth = ((HttpServletRequest) request).getHeader("Authorization");
        Long userId=Validations.validateToken(auth);
        request.setAttribute("userId", userId);
        // Permission
        chain.doFilter(request, response);
    }
}