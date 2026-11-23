
package in.strikes.devTrack.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

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

    private static final int MAX_VERIFICATION_ATTEMPTS = 5;

    private final PendingRegistrationRepository pendingRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailJsEmailService emailJsEmailService;

    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(
            PendingRegistrationRepository pendingRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailJsEmailService emailJsEmailService) {

        this.pendingRepository = pendingRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailJsEmailService = emailJsEmailService;
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

        // Remove an older pending registration for this email.
        pendingRepository.deleteByEmail(email);

        if (pendingRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Username is already being used in another pending registration");
        }

        // Generate a secure six-digit OTP.
        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        PendingRegistration pending = new PendingRegistration();

        pending.setUsername(username);
        pending.setEmail(email);
        pending.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );
        pending.setTokenHash(hashToken(otp));
        LocalDateTime now = LocalDateTime.now();

        pending.setExpiresAt(now.plusMinutes(10));
        pending.setOtpSentAt(now);
        pending.setVerificationAttempts(0);

        pendingRepository.save(pending);

        // Send the OTP using EmailJS.
        emailJsEmailService.sendVerificationEmail(
                email,
                username,
                otp
        );
    }
    @Transactional(noRollbackFor = IllegalArgumentException.class)
public void verifyEmail(String email, String otp) {

        if (email == null || email.isBlank()
                || otp == null || !otp.matches("\\d{6}")) {
            throw new IllegalArgumentException(
                    "A valid email and six-digit OTP are required"
            );
        }

        String normalizedEmail = email.trim().toLowerCase();

        PendingRegistration pending = pendingRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No pending registration found. Please register again."
                        )
                );

        if (!pending.getExpiresAt().isAfter(LocalDateTime.now())) {
            pendingRepository.delete(pending);
            throw new IllegalArgumentException(
                    "OTP has expired. Please register again."
            );
        }

        if (pending.getVerificationAttempts() >= MAX_VERIFICATION_ATTEMPTS) {
            pendingRepository.delete(pending);
            throw new IllegalArgumentException(
                    "Maximum verification attempts exceeded. Please register again."
            );
        }

        pending.setVerificationAttempts(
                pending.getVerificationAttempts() + 1
        );

        if (!hashToken(otp).equals(pending.getTokenHash())) {
            throw new IllegalArgumentException("Incorrect OTP");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            pendingRepository.delete(pending);
            throw new IllegalArgumentException("Email already exists");
        }

        if (userRepository.existsByUsername(pending.getUsername())) {
            pendingRepository.delete(pending);
            throw new IllegalArgumentException("Username already exists");
        }

        // Create the actual account only after successful OTP verification.
        User user = new User(
                pending.getUsername(),
                pending.getEmail(),
                pending.getPasswordHash()
        );

        userRepository.save(user);

        // Delete the temporary registration.
        pendingRepository.delete(pending);
    }

    @Transactional
public void resendOtp(String email) {

    if (email == null || email.isBlank()) {
        throw new IllegalArgumentException("Email is required");
    }

    String normalizedEmail = email.trim().toLowerCase();

    PendingRegistration pending = pendingRepository
            .findByEmail(normalizedEmail)
            .orElseThrow(() -> new IllegalArgumentException(
                    "No pending registration found. Please register again."
            ));

    LocalDateTime now = LocalDateTime.now();

    // Enforce 60-second resend cooldown.
    if (pending.getOtpSentAt() != null
            && pending.getOtpSentAt().plusSeconds(60).isAfter(now)) {
        throw new IllegalArgumentException(
                "Please wait 60 seconds before requesting another OTP."
        );
    }

    // Generate a fresh six-digit OTP.
    String otp = String.format(
            "%06d",
            secureRandom.nextInt(1_000_000)
    );

    // Send the new OTP.
    emailJsEmailService.sendVerificationEmail(
            normalizedEmail,
            pending.getUsername(),
            otp
    );

    // Update the stored OTP only after the email service succeeds.
    pending.setTokenHash(hashToken(otp));
    pending.setOtpSentAt(now);
    pending.setExpiresAt(now.plusMinutes(10));
    pending.setVerificationAttempts(0);

    pendingRepository.save(pending);
}

    private String hashToken(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Unable to hash verification OTP",
                    exception
            );
        }
    }
}
