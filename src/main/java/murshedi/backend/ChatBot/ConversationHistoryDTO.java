package murshedi.backend.ChatBot;
import java.util.List;

public class ConversationHistoryDTO {
    private String threadId;
    private List<QuestionAnswer> history;

    public ConversationHistoryDTO(String threadId, List<QuestionAnswer> history) {
        this.threadId = threadId;
        this.history = history;
    }

    public String getThreadId() {
        return threadId;
    }

    public List<QuestionAnswer> getHistory() {
        return history;
    }
}