package docSharing.filter;

import docSharing.utils.Validations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class AuthorizationFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        String url = ((HttpServletRequest) request).getRequestURL().toString();
        System.out.println(url);
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (!url.contains("auth") && !url.contains("ws") && !url.contains("error") && !httpRequest.getMethod().equals(HttpMethod.OPTIONS.toString())) {

            if (httpRequest.getHeader("authorization") != null) {
                String token = httpRequest.getHeader("authorization");
                if (token == null)
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Could not find a token in the request");
                Long userId = Validations.validateToken(token);
                request.setAttribute("userId", userId);
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Could not find a token in the request");
            }
        }

        chain.doFilter(request, response);
    }
}