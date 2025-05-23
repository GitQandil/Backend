package murshedi.backend.ChatBot;

import murshedi.backend.Appuser.AppUser;
import murshedi.backend.Appuser.UserRepository;
import murshedi.backend.Authentication.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/history")
public class ConversationHistoryController {

    private final QuestionAnswerRepository questionAnswerRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    public ConversationHistoryController(QuestionAnswerRepository questionAnswerRepository, JwtUtil jwtUtil, UserRepository userRepository, ConversationRepository conversationRepository) {
        this.questionAnswerRepository = questionAnswerRepository;

        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
    }

    @GetMapping
    public ResponseEntity<?> getHistory(@RequestParam("id") String conversationId) {
        conversationId = conversationId.trim();

        System.out.println("Received conversationId: [" + conversationId + "]");

        // Fetch conversation entity
        Optional<Conversation> optionalConversation = conversationRepository.findById(conversationId);
        if (optionalConversation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conversation not found");
        }

        Conversation conversation = optionalConversation.get();
        List<QuestionAnswer> history = questionAnswerRepository.findByConversation_Id(conversationId);

        if (history.isEmpty()) {
            return ResponseEntity.ok("No conversations found for this ID.");
        }

        System.out.println("Found conversations: " + history.size());

        // Wrap history and threadId in a DTO

        System.out.println(conversation.getThreadId());
        ConversationHistoryDTO dto = new ConversationHistoryDTO(conversation.getThreadId(), history);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteConversation(@RequestBody Map<String, String> requestBody, @RequestHeader("Authorization") String token) {
        String conversationID = requestBody.get("conversationID").trim();
        System.out.println(conversationID + "chosen conversation");
        // Extract the user's email from the JWT token
        String email = jwtUtil.extractEmail(token.substring(7));

        // Find the user
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Retrieve the conversation
        Optional<Conversation> optionalConversation = conversationRepository.findById(conversationID);
        if (optionalConversation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conversation not found");
        }

        Conversation conversation = optionalConversation.get();

        // Check that the conversation belongs to the requesting user
        if (!conversation.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this conversation");
        }

        // Delete the conversation
        conversationRepository.delete(conversation);

        return ResponseEntity.ok("Conversation deleted successfully");

    }

    @PostMapping("/deleteAll")
    public ResponseEntity<String> deleteAllConversations(@RequestHeader("Authorization") String token) {
        // Extract the user's email from the JWT token
        String email = jwtUtil.extractEmail(token.substring(7));

        // Find the user
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch all conversations of the user
        List<Conversation> userConversations = conversationRepository.findByUser(user);

        if (userConversations.isEmpty()) {
            return ResponseEntity.ok("No conversations to delete.");
        }

        // Delete them all
        conversationRepository.deleteAll(userConversations);

        return ResponseEntity.ok("All conversations have been deleted.");
    }

}
