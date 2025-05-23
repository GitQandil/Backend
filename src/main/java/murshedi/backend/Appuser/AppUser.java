package murshedi.backend.Appuser;

import murshedi.backend.ChatBot.Conversation;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@Entity
public class AppUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    public String getPassword() {
        return password;
    }

    private String password;
    private String email;

    @Enumerated(EnumType.STRING)
    private AppUserRole appUserRole;

    private boolean enabled;
    private boolean locked;
    private boolean blindMode;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Conversation> conversations = new ArrayList<>(); // Add Conversations list

    public AppUser() {}

    public AppUser(String firstName, String lastName, String password, String email, AppUserRole appUserRole, boolean blindMode) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.email = email;
        this.blindMode = blindMode;
        this.appUserRole = appUserRole;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(appUserRole.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return !locked; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public AppUserRole getAppUserRole() {
        return appUserRole;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isBlindMode() {
        return blindMode;
    }

    public List<Conversation> getConversations() {
        return conversations;
    }

    @Override
    public boolean isEnabled() { return enabled; }

}
