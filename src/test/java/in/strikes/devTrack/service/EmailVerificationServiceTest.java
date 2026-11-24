
package in.strikes.devTrack.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import in.strikes.devTrack.dto.RegisterRequest;
import in.strikes.devTrack.entity.PendingRegistration;
import in.strikes.devTrack.entity.User;
import in.strikes.devTrack.repository.PendingRegistrationRepository;
import in.strikes.devTrack.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private PendingRegistrationRepository pendingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailJsEmailService emailJsEmailService;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private PendingRegistration pending;

    @BeforeEach
    void setUp() {
        pending = new PendingRegistration();
        pending.setUsername("buddy123");
        pending.setEmail("buddy@example.com");
        pending.setPasswordHash("encoded-password");
        pending.setTokenHash(hash("123456"));
        pending.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        pending.setOtpSentAt(LocalDateTime.now().minusSeconds(70));
        pending.setVerificationAttempts(0);
    }

    @Test
    void startRegistrationShouldCreatePendingRegistrationAndSendOtp() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("buddy123");
        request.setEmail("BUDDY@example.com");
        request.setPassword("Password123");

        when(userRepository.existsByUsername("buddy123"))
                .thenReturn(false);
        when(userRepository.existsByEmail("buddy@example.com"))
                .thenReturn(false);
        when(pendingRepository.existsByUsername("buddy123"))
                .thenReturn(false);
        when(passwordEncoder.encode("Password123"))
                .thenReturn("encoded-password");

        emailVerificationService.startRegistration(request);

        ArgumentCaptor<PendingRegistration> pendingCaptor =
                ArgumentCaptor.forClass(PendingRegistration.class);

        verify(pendingRepository).save(pendingCaptor.capture());

        PendingRegistration saved = pendingCaptor.getValue();

        assertEquals("buddy123", saved.getUsername());
        assertEquals("buddy@example.com", saved.getEmail());
        assertEquals("encoded-password", saved.getPasswordHash());
        assertEquals(0, saved.getVerificationAttempts());
        assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now()));

        ArgumentCaptor<String> otpCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(emailJsEmailService).sendVerificationEmail(
                eq("buddy@example.com"),
                eq("buddy123"),
                otpCaptor.capture()
        );

        assertTrue(otpCaptor.getValue().matches("\\d{6}"));
        assertEquals(hash(otpCaptor.getValue()), saved.getTokenHash());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verifyEmailShouldCreateUserWhenOtpIsCorrect() {

        when(pendingRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(pending));

        when(userRepository.existsByEmail("buddy@example.com"))
                .thenReturn(false);

        when(userRepository.existsByUsername("buddy123"))
                .thenReturn(false);

        emailVerificationService.verifyEmail(
                "BUDDY@example.com",
                "123456"
        );

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        assertEquals("buddy123", userCaptor.getValue().getUsername());
        assertEquals("buddy@example.com", userCaptor.getValue().getEmail());

        verify(pendingRepository).delete(pending);
    }

    @Test
    void verifyEmailShouldRejectIncorrectOtp() {

        when(pendingRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(pending));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> emailVerificationService.verifyEmail(
                        "buddy@example.com",
                        "654321"
                )
        );

        assertEquals("Incorrect OTP", exception.getMessage());
        assertEquals(1, pending.getVerificationAttempts());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verifyEmailShouldRejectExpiredOtp() {

        pending.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(pendingRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(pending));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> emailVerificationService.verifyEmail(
                        "buddy@example.com",
                        "123456"
                )
        );

        assertEquals(
                "OTP has expired. Please register again.",
                exception.getMessage()
        );

        verify(pendingRepository).delete(pending);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resendOtpShouldRejectRequestDuringCooldown() {

        pending.setOtpSentAt(LocalDateTime.now().minusSeconds(20));

        when(pendingRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(pending));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> emailVerificationService.resendOtp(
                        "buddy@example.com"
                )
        );

        assertTrue(exception.getMessage().contains("60 seconds"));

        verify(emailJsEmailService, never())
                .sendVerificationEmail(any(), any(), any());
    }

    @Test
    void resendOtpShouldSendNewOtpAfterCooldown() {

        pending.setVerificationAttempts(3);
        pending.setOtpSentAt(LocalDateTime.now().minusSeconds(70));

        when(pendingRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(pending));

        emailVerificationService.resendOtp("buddy@example.com");

        ArgumentCaptor<String> otpCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(emailJsEmailService).sendVerificationEmail(
                eq("buddy@example.com"),
                eq("buddy123"),
                otpCaptor.capture()
        );

        String newOtp = otpCaptor.getValue();

        assertTrue(newOtp.matches("\\d{6}"));
        assertEquals(hash(newOtp), pending.getTokenHash());
        assertEquals(0, pending.getVerificationAttempts());
        assertTrue(pending.getExpiresAt().isAfter(LocalDateTime.now()));

        verify(pendingRepository).save(pending);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] result = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(result);

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
