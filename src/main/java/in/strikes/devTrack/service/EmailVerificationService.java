
package in.strikes.devTrack.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.strikes.devTrack.dto.RegisterRequest;
import in.strikes.devTrack.entity.PendingRegistration;
import in.strikes.devTrack.entity.User;
import in.strikes.devTrack.repository.PendingRegistrationRepository;
import in.strikes.devTrack.repository.UserRepository;

@Service
public class EmailVerificationService {

    private final PendingRegistrationRepository pendingRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResendEmailService resendEmailService;
    private final String frontendUrl;

    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(
            PendingRegistrationRepository pendingRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ResendEmailService resendEmailService,
            @Value("${app.frontend-url}") String frontendUrl) {

        this.pendingRepository = pendingRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.resendEmailService = resendEmailService;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public void startRegistration(RegisterRequest request) {

        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Remove an earlier pending registration for this email.
        pendingRepository.deleteByEmail(email);

        if (pendingRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Username is already being used in another pending registration");
        }

        // Generate a secure, random verification token.
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        PendingRegistration pending = new PendingRegistration();

        pending.setUsername(username);
        pending.setEmail(email);
        pending.setPasswordHash(
                passwordEncoder.encode(request.getPassword()));

        pending.setTokenHash(hashToken(rawToken));
        pending.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        pendingRepository.save(pending);

        String verificationLink =
                frontendUrl + "/verify-email?token=" + rawToken;

        String html = """
                <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2>Verify your DevTrack email</h2>
                    <p>Thank you for registering with DevTrack.</p>
                    <p>Click the button below to verify your email and create your account.</p>
                    <a href="%s"
                       style="display:inline-block;padding:12px 20px;
                       background:#2563eb;color:white;text-decoration:none;
                       border-radius:6px;">
                       Verify Email
                    </a>
                    <p>This link expires in 15 minutes.</p>
                    <p>If you did not request this, you can ignore this email.</p>
                </div>
                """.formatted(verificationLink);

        resendEmailService.sendEmail(
                email,
                "Verify your DevTrack email",
                html
        );
    }

    @Transactional
    public void verifyEmail(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Verification token is required");
        }

        String tokenHash = hashToken(rawToken);

        PendingRegistration pending = pendingRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid or expired verification link"));

        if (!pending.getExpiresAt().isAfter(LocalDateTime.now())) {
            pendingRepository.delete(pending);
            throw new IllegalArgumentException(
                    "Verification link has expired. Please register again.");
        }

        if (userRepository.existsByEmail(pending.getEmail())) {
            pendingRepository.delete(pending);
            throw new IllegalArgumentException("Email already exists");
        }

        if (userRepository.existsByUsername(pending.getUsername())) {
            pendingRepository.delete(pending);
            throw new IllegalArgumentException("Username already exists");
        }

        // Create the actual account only after successful verification.
        User user = new User(
                pending.getUsername(),
                pending.getEmail(),
                pending.getPasswordHash()
        );

        userRepository.save(user);

        // Remove the temporary registration.
        pendingRepository.delete(pending);
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Unable to generate verification token hash",
                    exception
            );
        }
    }
}
