package murshedi.backend.Authentication;

import murshedi.backend.Appuser.AppUserRole;
import murshedi.backend.ChatBot.ConversationDTO;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"token", "message", "firstName", "lastName", "email", "blind", "conversations"})
public class AuthResponse {

    private String token;
    private String message;
    private String firstName;
    private String lastName;
    private String email;
    private boolean blind;
    private List<ConversationDTO> conversations;
    private AppUserRole appUserRole;
    private Long id;


    public AuthResponse(String token, String message, String firstName, String lastName, String email, boolean blindMode, List<ConversationDTO> conversations, Long id, AppUserRole appUserRole) {
        this.token = token;
        this.message = message;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.blind = blindMode;
        this.conversations = conversations;
        this.id = id;
        this.appUserRole = appUserRole;
    }



    // Add getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isBlind() {
        return blind;
    }

    public String getEmail() {
        return email;
    }

    public List<ConversationDTO> getConversations() {
        return conversations;
    }

    public AppUserRole getAppUserRole() {
        return appUserRole;
    }

    public void setAppUserRole(AppUserRole appUserRole) {
        this.appUserRole = appUserRole;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}