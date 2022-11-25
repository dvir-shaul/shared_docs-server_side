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