
package in.strikes.devTrack.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.password.PasswordEncoder;

import in.strikes.devTrack.entity.User;
import in.strikes.devTrack.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class GitHubOAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private GitHubOAuthService gitHubOAuthService;

    private OAuth2User createGitHubUser(String email) {
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("id", 12345, "email", email),
                "id"
        );
    }

    @Test
    void shouldRejectGitHubUserWithoutEmail() {
        OAuth2User oauthUser = createGitHubUser("");

        assertThrows(
                IllegalArgumentException.class,
                () -> gitHubOAuthService.processGitHubUser(oauthUser)
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

        String token = gitHubOAuthService.processGitHubUser(
                createGitHubUser("buddy@example.com")
        );

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

        String token = gitHubOAuthService.processGitHubUser(
                createGitHubUser("buddy@example.com")
        );

        assertEquals("mock-jwt-token", token);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("buddy@example.com", savedUser.getEmail());
        assertTrue(savedUser.getUsername().startsWith("buddy_"));
        assertTrue(savedUser.getUsername().matches("buddy_\\d{6}"));
        assertEquals(
                "encoded-random-password",
                savedUser.getPasswordHash()
        );

        verify(passwordEncoder).encode(anyString());
        verify(jwtService).generateToken("buddy@example.com");
    }

    @Test
    void shouldNormalizeEmailBeforeLookingUpUser() {
        User existingUser = new User(
                "buddy123",
                "buddy@example.com",
                "password-hash"
        );

        when(userRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(existingUser));

        when(jwtService.generateToken("buddy@example.com"))
                .thenReturn("mock-jwt-token");

        String token = gitHubOAuthService.processGitHubUser(
                createGitHubUser("  BUDDY@EXAMPLE.COM  ")
        );

        assertEquals("mock-jwt-token", token);

        verify(userRepository).findByEmail("buddy@example.com");
        verify(jwtService).generateToken("buddy@example.com");
    }
}
