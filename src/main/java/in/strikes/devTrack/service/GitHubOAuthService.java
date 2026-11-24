
package in.strikes.devTrack.service;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.strikes.devTrack.entity.User;
import in.strikes.devTrack.repository.UserRepository;

@Service
public class GitHubOAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final SecureRandom secureRandom = new SecureRandom();

    public GitHubOAuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public String processGitHubUser(OAuth2User oauthUser) {

        String email = oauthUser.getAttribute("email");

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "GitHub account does not have a verified email address."
            );
        }

        email = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = createGitHubUser(email);
        }

        return jwtService.generateToken(user.getEmail());
    }

    private User createGitHubUser(String email) {

        String username = generateUniqueUsername(email);

        String randomPassword = UUID.randomUUID().toString();

        User user = new User(
                username,
                email,
                passwordEncoder.encode(randomPassword)
        );

        return userRepository.save(user);
    }

    private String generateUniqueUsername(String email) {

        String base = email.substring(0, email.indexOf('@'))
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]", "");

        if (base.isBlank()) {
            base = "githubuser";
        }

        base = base.substring(0, Math.min(base.length(), 42));

        String username;

        do {
            int suffix = secureRandom.nextInt(1_000_000);
            username = base + "_" + String.format("%06d", suffix);
        } while (userRepository.existsByUsername(username));

        return username;
    }
}
