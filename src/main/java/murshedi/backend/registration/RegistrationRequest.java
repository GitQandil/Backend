package murshedi.backend.registration;


import lombok.*;

@Getter
@EqualsAndHashCode
@ToString
public class RegistrationRequest {
    private final String firstName;
    private final String lastName;
    private final String password;
    private final String email;
    private final boolean blindMode;

    public RegistrationRequest(String firstName, String lastName, String password, String email, boolean blindMode) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.email = email;
        this.blindMode = blindMode;
    }

    public boolean isBlindMode() {
        return blindMode;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}
