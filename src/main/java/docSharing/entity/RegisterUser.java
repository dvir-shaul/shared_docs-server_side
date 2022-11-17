package docSharing.entity;

public class RegisterUser {
    private final String email;
    private final String name;
    private final String password;

    public RegisterUser(String email, String name, String password){
        this.email = email;
        this.password = password;
        this.name = name;
    }
}
