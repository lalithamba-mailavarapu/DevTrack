
package in.strikes.devTrack.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import in.strikes.devTrack.entity.PasswordResetToken;
import in.strikes.devTrack.entity.User;
import in.strikes.devTrack.repository.PasswordResetTokenRepository;
import in.strikes.devTrack.repository.UserRepository;

@Service
public class PasswordResetService {

    private static final Logger logger =
            LoggerFactory.getLogger(PasswordResetService.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final ResendEmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url}")
    private String frontendUrl;

   public PasswordResetService(
        UserRepository userRepository,
        PasswordResetTokenRepository tokenRepository,
        ResendEmailService emailService,
        PasswordEncoder passwordEncoder) {

    this.userRepository = userRepository;
    this.tokenRepository = tokenRepository;
    this.emailService = emailService;
    this.passwordEncoder = passwordEncoder;
}

    @Transactional
    public void requestPasswordReset(String email) {

        User user = userRepository.findByEmail(email.trim())
                .orElse(null);

        // Do not reveal whether the email exists.
        if (user == null) {
            return;
        }

        // Invalidate previous reset tokens.
        tokenRepository.deleteByUserId(user.getId());

        // Generate a secure random token.
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // Store only the SHA-256 hash.
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = new PasswordResetToken(
                tokenHash,
                user,
                LocalDateTime.now().plusMinutes(15)
        );

        tokenRepository.save(resetToken);

        String resetLink = frontendUrl.replaceAll("/+$", "")
                + "/reset-password?token="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);

        String html = """
                <h2>Reset your DevTrack password</h2>
                <p>We received a request to reset your password.</p>
                <p>
                    <a href="%s">Reset Password</a>
                </p>
                <p>This link expires in 15 minutes.</p>
                <p>If you did not request this, you can ignore this email.</p>
                """.formatted(resetLink);

        try {
            emailService.sendEmail(
                    user.getEmail(),
                    "Reset your DevTrack password",
                    html
            );
        } catch (RestClientException exception) {
            // Never log the reset token or email credentials.
            logger.warn("Password reset email delivery failed.");
        }
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Unable to generate password reset token hash",
                    exception
            );
        }
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {

        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = tokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid or expired reset link"
                ));

        if (resetToken.getUsedAt() != null ||
                resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Invalid or expired reset link"
            );
        }

        User user = resetToken.getUser();

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);
    }

}
