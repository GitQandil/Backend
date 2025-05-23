package murshedi.backend.ChatBot;

import murshedi.backend.Appuser.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;

    @Autowired
    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    // Fetch conversations for a specific user
    public List<Conversation> getConversationsByUser(AppUser user) {
        List<Conversation> conversations = conversationRepository.findByUser(user);
        System.out.println("Fetched conversations: " + conversations); // Add logging
        return conversations;
    }
}
