package murshedi.backend.Appuser;

import murshedi.backend.Authentication.JwtUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service

public class UserService implements UserDetailsService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    public UserService(JwtUtil jwtUtil, UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User not found with email " + email)
                );
    }

    public boolean findBlindByEmail(String email) {
        boolean blindModeOpt = userRepository.findBlindModeByEmail(email);

        boolean blindMode = blindModeOpt;
        return blindMode;
    }


    public String signUpUser(AppUser user) {
        boolean exist =userRepository.findByEmail(user.getEmail()).isPresent();

        if (exist) {
            throw new IllegalStateException("User already exists");
        }

        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        return token;
    }
    public int enableAppUser(String email) {
        return userRepository.enableAppUser(email);
    }
    public Boolean isEnabled(String email) {
        Optional<AppUser> user = userRepository.findByEmail(email);
        return user.map(AppUser::isEnabled).orElse(false);
    }
}
