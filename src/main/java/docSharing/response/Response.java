package docSharing.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class Response {
    @JsonProperty("message")
    private String message;
    @JsonProperty("status")
    private HttpStatus status;
    @JsonProperty("data")
    private Object data;
    @JsonProperty("statusCode")
    private Integer statusCode;


    public static class Builder {

        private String message;
        private HttpStatus status;
        private Integer statusCode;
        private Object data;

        public Builder() {
            this.status = null;

            this.message = null;
            this.data = null;
            this.statusCode = null;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder status(HttpStatus status) {
            this.status = status;
            return this;
        }

        public Builder statusCode(Integer statusCode){
            this.statusCode = statusCode;
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