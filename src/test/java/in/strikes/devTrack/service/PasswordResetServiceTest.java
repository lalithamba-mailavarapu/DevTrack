
package in.strikes.devTrack.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;

import in.strikes.devTrack.entity.PasswordResetToken;
import in.strikes.devTrack.entity.User;
import in.strikes.devTrack.repository.PasswordResetTokenRepository;
import in.strikes.devTrack.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private ResendEmailService emailService;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                passwordResetService,
                "frontendUrl",
                "https://devtrack-frontend-i5ze.onrender.com/"
        );

        user = new User(
                "buddy123",
                "buddy@example.com",
                "old-password-hash"
        );
    }

    @Test
    void requestPasswordResetShouldDoNothingForUnknownEmail() {
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        passwordResetService.requestPasswordReset("unknown@example.com");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void requestPasswordResetShouldSaveHashedTokenAndSendEmail() {
        when(userRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(user));

        passwordResetService.requestPasswordReset(" buddy@example.com ");

        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);

        verify(tokenRepository).deleteByUserId(user.getId());
        verify(tokenRepository).save(tokenCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();

        assertNotNull(savedToken.getTokenHash());
        assertNotEquals("", savedToken.getTokenHash());
        assertEquals(user, savedToken.getUser());
        assertTrue(savedToken.getExpiresAt().isAfter(LocalDateTime.now()));
        assertTrue(savedToken.getExpiresAt().isBefore(
                LocalDateTime.now().plusMinutes(16)
        ));

        ArgumentCaptor<String> htmlCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(emailService).sendEmail(
                eq("buddy@example.com"),
                eq("Reset your DevTrack password"),
                htmlCaptor.capture()
        );

        String html = htmlCaptor.getValue();

        assertTrue(html.contains("https://devtrack-frontend-i5ze.onrender.com/reset-password?token="));
        assertTrue(html.contains("This link expires in 15 minutes."));
    }

    @Test
    void requestPasswordResetShouldNotThrowWhenEmailDeliveryFails() {
        when(userRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(user));

        doThrow(new RestClientException("Email service unavailable"))
                .when(emailService)
                .sendEmail(anyString(), anyString(), anyString());

        assertDoesNotThrow(() ->
                passwordResetService.requestPasswordReset("buddy@example.com")
        );

        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void resetPasswordShouldUpdatePasswordAndMarkTokenAsUsed() {
        String rawToken = "valid-reset-token";
        PasswordResetToken resetToken = new PasswordResetToken(
                hashToken(rawToken),
                user,
                LocalDateTime.now().plusMinutes(10)
        );

        when(tokenRepository.findByTokenHash(hashToken(rawToken)))
                .thenReturn(Optional.of(resetToken));

        when(passwordEncoder.encode("newPassword123"))
                .thenReturn("new-password-hash");

        passwordResetService.resetPassword(rawToken, "newPassword123");

        assertEquals("new-password-hash", user.getPasswordHash());
        assertNotNull(resetToken.getUsedAt());

        verify(userRepository).save(user);
        verify(tokenRepository).save(resetToken);
        verify(passwordEncoder).encode("newPassword123");
    }

    @Test
    void resetPasswordShouldRejectInvalidToken() {
        String rawToken = "invalid-token";

        when(tokenRepository.findByTokenHash(hashToken(rawToken)))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.resetPassword(rawToken, "newPassword123")
        );

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void resetPasswordShouldRejectExpiredToken() {
        String rawToken = "expired-token";

        PasswordResetToken resetToken = new PasswordResetToken(
                hashToken(rawToken),
                user,
                LocalDateTime.now().minusMinutes(1)
        );

        when(tokenRepository.findByTokenHash(hashToken(rawToken)))
                .thenReturn(Optional.of(resetToken));

        assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.resetPassword(rawToken, "newPassword123")
        );

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void resetPasswordShouldRejectAlreadyUsedToken() {
        String rawToken = "already-used-token";

        PasswordResetToken resetToken = new PasswordResetToken(
                hashToken(rawToken),
                user,
                LocalDateTime.now().plusMinutes(10)
        );

        resetToken.setUsedAt(LocalDateTime.now().minusMinutes(1));

        when(tokenRepository.findByTokenHash(hashToken(rawToken)))
                .thenReturn(Optional.of(resetToken));

        assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.resetPassword(rawToken, "newPassword123")
        );

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
