package murshedi.backend.ChatBot;

import murshedi.backend.Appuser.AppUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;


import java.util.ArrayList;
import java.util.List;
@Entity
public class Conversation {
    @Id
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private String id;  // ✅ Ensure this is a String (UUID)

    private String title;  // Optional: Can store "New Conversation" or user-defined title

    @Column(name = "thread_id")
    private String threadId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private AppUser user;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuestionAnswer> questionAnswers = new ArrayList<>();

    public Conversation() {}

    public Conversation(AppUser user, String title, String id) {
        this.user = user;
        this.title = title;
        this.id = id;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) {this.user = user;}
    public String getThreadId() { return threadId; }


    public void setThreadId(String threadId) { this.threadId = threadId; }

}


