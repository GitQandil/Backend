package murshedi.backend.registration;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "api/v1/registration")
@CrossOrigin(origins = "http://localhost:5173")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }


    @PostMapping
    public ResponseEntity<Map<String, String>> register(@RequestBody RegistrationRequest request) {
        return registrationService.register(request);
    }

    @GetMapping(path = "confirm")
    public String confirm(@RequestParam("token") String token) {
        return registrationService.confirmToken(token);
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping(path = "user/status")
    public ResponseEntity<Map<String, Boolean>> checkActivationStatus(@RequestParam("email") String email) {
        System.out.println(email);
        boolean isActivated = registrationService.isEnabled(email); // Implement this method in UserService
        Map<String, Boolean> response = new HashMap<>();
        response.put("activated", isActivated);
        System.out.println(email);
        System.out.println(isActivated);
        return ResponseEntity.ok(response);
    }
 }
