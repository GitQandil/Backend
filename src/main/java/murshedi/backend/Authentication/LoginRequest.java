package murshedi.backend.Authentication;

public class LoginRequest {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String username) {
        this.email = username;
    }


}
