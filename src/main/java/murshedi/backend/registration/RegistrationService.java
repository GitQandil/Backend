package murshedi.backend.registration;

import murshedi.backend.Appuser.AppUser;
import murshedi.backend.Appuser.AppUserRole;
import murshedi.backend.Appuser.UserService;
import murshedi.backend.Authentication.JwtUtil;
import murshedi.backend.email.EmailSender;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class RegistrationService {  // Fixed typo

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final EmailSender emailSender;

    public RegistrationService(JwtUtil jwtUtil, UserService userService, EmailSender emailSender) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.emailSender = emailSender;
            }

    public ResponseEntity<Map<String, String>> register(RegistrationRequest request) {

        String token = userService.signUpUser( new AppUser(
                request.getFirstName(),
                request.getLastName(),
                request.getPassword(),
                request.getEmail(),
                AppUserRole.USER,
                request.isBlindMode()
         )
        );

        String link = "http://localhost:8080/api/v1/registration/confirm?token=" + token;
        emailSender.send(
                request.getEmail(),
                "Confirm your email",
                buildEmail(request.getFirstName(), link)
        );


        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @Transactional
    public String confirmToken(String token) {
        // Validate the JWT
        if (!jwtUtil.isTokenValid(token, jwtUtil.extractEmail(token))) {
            throw new IllegalStateException("Invalid or expired token");
        }

        // Extract the user's email from the JWT
        String userEmail = jwtUtil.extractEmail(token);


        // Enable the user's account
        userService.enableAppUser(userEmail);


        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("confirmation", "Confirmed");
        return response.toString();
    }

    public boolean isEnabled(String email) {
       return userService.isEnabled(email);
    }

    private String buildEmail(String name, String link) {
        return "<div style=\"font-family: Arial, sans-serif; font-size: 16px; color: #333; background-color: #f4f4f4; padding: 20px;\">"
                + "<table align=\"center\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width: 600px; background: #ffffff; border-radius: 8px; padding: 20px;\">"
                + "  <tr>"
                + "    <td style=\"text-align: center; padding-bottom: 20px;\">"
                + "      <h2 style=\"color: #1D70B8; margin: 0;\">Confirm Your Email</h2>"
                + "    </td>"
                + "  </tr>"
                + "  <tr>"
                + "    <td style=\"padding: 10px 20px;\">"
                + "      <p>Hi " + name + ",</p>"
                + "      <p>Thank you for registering. Please click the button below to activate your account:</p>"
                + "      <div style=\"text-align: center; margin: 20px 0;\">"
                + "        <a href=\"" + link + "\" style=\"background-color: #1D70B8; color: #ffffff; padding: 12px 20px; text-decoration: none; font-weight: bold; border-radius: 5px; display: inline-block;\">Activate Now</a>"
                + "      </div>"
                + "      <p style=\"color: #777; font-size: 14px;\">This link will expire in 15 minutes.</p>"
                + "      <p>See you soon!</p>"
                + "    </td>"
                + "  </tr>"
                + "  <tr>"
                + "    <td style=\"text-align: center; padding-top: 20px; font-size: 12px; color: #888;\">"
                + "      &copy; 2025 YourCompany. All rights reserved."
                + "    </td>"
                + "  </tr>"
                + "</table>"
                + "</div>";
    }

}
