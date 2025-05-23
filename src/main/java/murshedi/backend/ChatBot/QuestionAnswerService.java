package murshedi.backend.ChatBot;

import murshedi.backend.Appuser.AppUser;
import murshedi.backend.Appuser.UserRepository;
import murshedi.backend.ChatBot.dto.AssistantRequestDto;
import murshedi.backend.ChatBot.dto.AssistantResponseDto;
import murshedi.backend.ChatBot.service.AssistantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuestionAnswerService {

    private final QuestionAnswerRepository questionAnswerRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final AssistantService assistantService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public QuestionAnswerService(QuestionAnswerRepository questionAnswerRepository,
                                 ConversationRepository conversationRepository, 
                                 UserRepository userRepository,
                                 AssistantService assistantService) {
        this.questionAnswerRepository = questionAnswerRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.assistantService = assistantService;
    }

    public ResponseEntity<Map<String, String>> getAnswerFromFlaskAPI(String userQuestion, String conversationId, String email, String thread_id) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Conversation conversation;

        if (conversationId == null || !conversationRepository.existsById(conversationId)) {
            conversationId = conversationId != null ? conversationId : UUID.randomUUID().toString();
            conversation = new Conversation(user, userQuestion, conversationId);
        } else {
            conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
        }

        boolean blind = userRepository.findBlindModeByEmail(email);
        String responseId = null;

        // Use Spring Boot Assistant Service instead of Flask API
        AssistantRequestDto requestDto = new AssistantRequestDto();
        requestDto.setQuestion(userQuestion);
        requestDto.setThreadId(thread_id);
        requestDto.setBlindMode(blind);
        
        AssistantResponseDto assistantResponse = assistantService.processQuestion(requestDto);
        
        String answerAsJson = assistantResponse.getAnswer();
        responseId = assistantResponse.getResponseId();
        String returnedThreadId = assistantResponse.getThreadId();

        // Save thread_id only if not already stored
        if (conversation.getThreadId() == null && returnedThreadId != null) {
            conversation.setThreadId(returnedThreadId);
        }

        conversationRepository.save(conversation);

        QuestionAnswer questionAnswer = new QuestionAnswer();
        questionAnswer.setQuestion(userQuestion);
        questionAnswer.setAnswer(answerAsJson);
        questionAnswer.setConversation(conversation);
        questionAnswer.setResponseID(responseId);
        questionAnswerRepository.save(questionAnswer);

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("answer", answerAsJson);
        responseMap.put("thread_id", returnedThreadId);
        responseMap.put("conversationId", conversation.getId());
        responseMap.put("response_id", responseId);
        return ResponseEntity.ok(responseMap);
    }
}
