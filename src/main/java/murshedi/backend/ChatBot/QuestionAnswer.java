package murshedi.backend.ChatBot;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
@Entity
public class QuestionAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1024)  // Increase the size of the 'question' column
    private String question;

    @Column(columnDefinition = "TEXT")  // Use TEXT for longer 'answer' columns
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore // 🔥 This prevents infinite recursion when returning JSON
    @JoinColumn(name = "conversation_id", referencedColumnName = "id") // Ensure correct mapping
    private Conversation conversation;

    @Column(length = 1024)
    private String ResponseID;


    public void setQuestion(String question) { this.question = question; }
    public void setAnswer(String answer) { this.answer = answer; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }



    public String getAnswer() { return answer; }
    public Long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }


    public String getResponseID() {
        return ResponseID;
    }

    public void setResponseID(String responseID) {
        ResponseID = responseID;
    }
}
