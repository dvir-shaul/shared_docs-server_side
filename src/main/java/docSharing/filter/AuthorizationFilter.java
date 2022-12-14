package docSharing.filter;

import docSharing.service.AuthService;
import docSharing.utils.ExceptionMessage;
import docSharing.utils.Validations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.server.ResponseStatusException;

import javax.security.auth.login.AccountNotFoundException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class AuthorizationFilter extends GenericFilterBean {

    @Autowired
    AuthService authService;

    /**
     * this doFilter function is set to check if the user has the permission to enter the app controllers.
     * checks if the request was according to what we need with token in the authorization Header.
     *
     * @param request  - request from client
     * @param response - response if the action can be done or not.
     * @param chain    - chain of filters to go through
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        logger.info("in AuthorizationFilter -> doFilter");
        String url = ((HttpServletRequest) request).getRequestURL().toString();
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (!url.contains("auth") && !url.contains("ws") && !url.contains("error") && !httpRequest.getMethod().equals(HttpMethod.OPTIONS.toString())) {

            if (httpRequest.getHeader("authorization") != null) {
                String token = httpRequest.getHeader("authorization");
                if (token == null) {
                    logger.error("in AuthorizationFilter -> doFilter -> token is null");
                    ((HttpServletResponse) response).setStatus(400);
                    response.getOutputStream().write(ExceptionMessage.TOKEN_IS_NULL.toString().getBytes());
                    return;
                }
                try {
                    Long userId = authService.checkTokenToUserInDB(token);
                    request.setAttribute("userId", userId);
                } catch (AccountNotFoundException e) {
                    logger.error("in AuthorizationFilter -> doFilter -> " + e.getMessage());
                    //Servers send 404 instead of 403 Forbidden to hide the existence
                    // of a resource from an unauthorized client.
                    ((HttpServletResponse) response).setStatus(404);
                    response.getOutputStream().write(ExceptionMessage.NO_USER_IN_DATABASE.toString().getBytes());
                    return;
                }
            } else {
                logger.error("in AuthorizationFilter -> doFilter -> Could not find a token in the request");
                ((HttpServletResponse) response).setStatus(400);
                response.getOutputStream().write(ExceptionMessage.WRONG_SEARCH.toString().getBytes());
                return;
            }
        }
        chain.doFilter(request, response);
    }
}