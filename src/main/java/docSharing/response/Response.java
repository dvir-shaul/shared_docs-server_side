package docSharing.response;

import org.springframework.http.HttpStatus;

public class Response<T> {

    private String message;
    private HttpStatus status;
    private T data;

}
