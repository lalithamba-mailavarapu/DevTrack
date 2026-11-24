
package in.strikes.devTrack.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import in.strikes.devTrack.entity.User;
import in.strikes.devTrack.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class GoogleOAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private GoogleOAuthService googleOAuthService;

    private OAuth2User googleUser;

    @BeforeEach
    void setUp() {
        googleUser = new DefaultOAuth2User(
                java.util.List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "email", "buddy@example.com",
                        "email_verified", true
                ),
                "email"
        );
    }

    @Test
    void shouldRejectGoogleUserWithoutEmail() {
        OAuth2User user = new DefaultOAuth2User(
                java.util.List.of(),
                Map.of("email_verified", true),
                "email_verified"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> googleOAuthService.processGoogleUser(user)
        );

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void shouldRejectGoogleUserWithUnverifiedEmail() {
        OAuth2User user = new DefaultOAuth2User(
                java.util.List.of(),
                Map.of(
                        "email", "buddy@example.com",
                        "email_verified", false
                ),
                "email"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> googleOAuthService.processGoogleUser(user)
        );

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void shouldReturnTokenForExistingUser() {
        User existingUser = new User(
                "buddy123",
                "buddy@example.com",
                "existing-password-hash"
        );

        when(userRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken("buddy@example.com"))
        .thenReturn("mock-jwt-token");

        String token = googleOAuthService.processGoogleUser(googleUser);

        assertEquals("mock-jwt-token", token);

        verify(userRepository).findByEmail("buddy@example.com");
        verify(userRepository, never()).save(any());
        verify(jwtService).generateToken("buddy@example.com");
    }

    @Test
    void shouldCreateNewUserWhenEmailDoesNotExist() {
        when(userRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.empty());

        when(userRepository.existsByUsername(anyString()))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded-random-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtService.generateToken("buddy@example.com"))
        .thenReturn("mock-jwt-token");

        String token = googleOAuthService.processGoogleUser(googleUser);

        assertEquals("mock-jwt-token", token);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("buddy@example.com", savedUser.getEmail());
        assertTrue(savedUser.getUsername().startsWith("buddy_"));
        assertEquals(
                "encoded-random-password",
                savedUser.getPasswordHash()
        );

        verify(jwtService).generateToken("buddy@example.com");
    }

    @Test
    void shouldNormalizeEmailBeforeLookingUpExistingUser() {
        OAuth2User user = new DefaultOAuth2User(
                java.util.List.of(),
                Map.of(
                        "email", "  BUDDY@EXAMPLE.COM  ",
                        "email_verified", true
                ),
                "email"
        );

        User existingUser = new User(
                "buddy123",
                "buddy@example.com",
                "password-hash"
        );

        when(userRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(existingUser));

        when(jwtService.generateToken("buddy@example.com"))
        .thenReturn("mock-jwt-token");

        String token = googleOAuthService.processGoogleUser(user);

        assertEquals("mock-jwt-token", token);

        verify(userRepository).findByEmail("buddy@example.com");
        verify(jwtService).generateToken("buddy@example.com");
    }
}
