package murshedi.backend.Authentication;

import murshedi.backend.Appuser.AppUser;
import murshedi.backend.Appuser.UserRepository;
import murshedi.backend.ChatBot.Conversation;
import murshedi.backend.ChatBot.ConversationDTO;
import murshedi.backend.ChatBot.ConversationService;
import murshedi.backend.email.EmailSender;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ConversationService conversationService;
    private UserRepository userRepository;
    private EmailSender emailSender;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, ConversationService conversationService, UserRepository userRepository, EmailSender emailSender, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.conversationService = conversationService;
        this.userRepository = userRepository;
        this.emailSender = emailSender;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        AppUser user = (AppUser) authentication.getPrincipal();

        // Fetch the user's conversations
        List<Conversation> conversations = conversationService.getConversationsByUser(user);

        // Map conversations to only include the ID and title
        List<ConversationDTO> conversationDTOs = conversations.stream()
                .map(conversation -> new ConversationDTO(conversation.getId(), conversation.getTitle()))
                .collect(Collectors.toList());

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        System.out.println(user.getAppUserRole());

        // Return response with token and conversation details
        return ResponseEntity.ok(new AuthResponse(
                token, "Login successful",
                user.getFirstName(), user.getLastName(),
                user.getEmail(), user.isBlindMode(),
               conversationDTOs, user.getId(),user.getAppUserRole())
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        Optional<AppUser> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        String token = jwtUtil.generateTokenWithExpiration(email, 15); // You’ll need to add this method

        String resetLink = "http://localhost:5173/resetpassword";
        String emailContent = "<p>Hi,</p>"
                + "<p>You requested a password reset. Click the link below to reset it:</p>"
                + "<a href=\"" + resetLink + "\">Reset Password</a>"
                + "<p>This link will expire in 10 minutes.</p>";

        emailSender.send(email, "Reset Your Password", emailContent);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
        }

        String email = jwtUtil.extractEmail(token);

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok("Password reset successful.");
    }
}
