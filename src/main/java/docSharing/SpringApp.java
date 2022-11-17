package docSharing;

import docSharing.Entities.User;
import docSharing.Utils.Activation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringApp {
    public static void main(String[] args) {

        SpringApplication.run(SpringApp.class, args);

    }
}