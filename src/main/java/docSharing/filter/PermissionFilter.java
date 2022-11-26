package docSharing.filter;

import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class PermissionFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        String url = ((HttpServletRequest) request).getRequestURL().toString();
        System.out.println(url);
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Permission

        chain.doFilter(request, response);
    }
}
