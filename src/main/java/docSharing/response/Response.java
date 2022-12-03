package docSharing.response;

import lombok.ToString;
import org.springframework.http.HttpStatus;

@ToString
public class Response {

    private String message;
    private HttpStatus status;
    private Object data;

    public static class Builder {

        private String message;
        private HttpStatus status;
        private Object data;

        public Builder() {
            this.status = null;
            this.message = null;
            this.data = null;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder status(HttpStatus status) {
            this.status = status;
            return this;
        }

        public <T> Builder data(Object data) {
            this.data = data;
            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }

    private Response(Builder builder) {
        this.status = builder.status;
        this.message = builder.message;
        this.data = builder.data;
    }
}