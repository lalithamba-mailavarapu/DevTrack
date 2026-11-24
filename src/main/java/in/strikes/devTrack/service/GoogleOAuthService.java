
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
public class GoogleOAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();

    public GoogleOAuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public String processGoogleUser(OAuth2User oauthUser) {

        String email = oauthUser.getAttribute("email");

        Boolean emailVerified =
                oauthUser.getAttribute("email_verified");

        if (email == null || email.isBlank()
                || !Boolean.TRUE.equals(emailVerified)) {

            throw new IllegalArgumentException(
                    "Google account does not have a verified email address."
            );
        }

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        // Reuse an existing account with the same verified email.
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> createGoogleUser(normalizedEmail));

        return jwtService.generateToken(user.getEmail());
    }

    private User createGoogleUser(String email) {

        String username = generateUniqueUsername(email);

        // Google users do not receive a usable local password.
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
            base = "googleuser";
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
