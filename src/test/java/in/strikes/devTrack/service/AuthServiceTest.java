
package in.strikes.devTrack.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import in.strikes.devTrack.dto.LoginRequest;
import in.strikes.devTrack.dto.LoginResponse;
import in.strikes.devTrack.entity.User;
import in.strikes.devTrack.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {

        User user = new User(
                "buddy",
                "buddy@example.com",
                "encoded-password"
        );

        LoginRequest request = new LoginRequest();
        request.setEmail("buddy@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                "encoded-password"
        )).thenReturn(true);

        when(jwtService.generateToken("buddy@example.com"))
                .thenReturn("test-jwt-token");

        LoginResponse response = authService.login(request);

        assertEquals("test-jwt-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());

        verify(jwtService).generateToken("buddy@example.com");
    }

    @Test
    void login_shouldThrowException_whenEmailDoesNotExist() {

        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void login_shouldThrowException_whenPasswordIsIncorrect() {

        User user = new User(
                "buddy",
                "buddy@example.com",
                "encoded-password"
        );

        LoginRequest request = new LoginRequest();
        request.setEmail("buddy@example.com");
        request.setPassword("wrong-password");

        when(userRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "encoded-password"
        )).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(jwtService, never()).generateToken(anyString());
    }
}
