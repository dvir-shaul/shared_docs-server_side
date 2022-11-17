package docSharing.controller;

import docSharing.service.AuthService;
import org.springframework.stereotype.Controller;

@Controller
public class AuthController {

    private AuthService authService;

    public void register(String email, String password, String name){
        // validate information
        // if correct -> call auth service with parameters -> register function
    }

    public void login(String email, String password){
        // validate information
        // if correct -> call authService with parameters -> login function
        // return token
    }

    public void activate(){
        // parse link to token
        // check if token is still activated
        // if yes -> call AuthService with activate function
        // if no do that -> resend email
    }


    private void validate(String email, String password, String name){
        // validate the received data
        // maybe call a util function to check each parameter?
    }
}
