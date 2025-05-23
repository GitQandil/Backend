package murshedi.backend.ChatBot;

import murshedi.backend.Appuser.UserRepository;
import murshedi.backend.Authentication.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api")
public class OpenAiController {

    private final QuestionAnswerService questionAnswerService;
    private final JwtUtil jwtUtil;


    public OpenAiController(QuestionAnswerService questionAnswerService, JwtUtil jwtUtil, UserRepository userRepository, UserRepository userRepository1, ConversationRepository conversationRepository) {
        this.questionAnswerService = questionAnswerService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/ask")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Map<String, String>> askQuestion(@RequestBody Map<String, String> request, @RequestHeader("Authorization") String token) {

        String userQuestion = request.get("question");
        String thread_id = request.get("thread_id");
        String conversationId = request.containsKey("conversationId") ? request.get("conversationId") : null;

        System.out.println(conversationId);


        if (userQuestion == null || userQuestion.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        String email = jwtUtil.extractEmail(token.substring(7));

        // Pass user email and conversationId
        return questionAnswerService.getAnswerFromFlaskAPI(userQuestion, conversationId, email,thread_id);
    }






}
