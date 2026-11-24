
package in.strikes.devTrack.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            Base64.getEncoder().encodeToString(
                    "01234567890123456789012345678901"
                            .getBytes()
            );

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 60000);
    }

    @Test
    void shouldGenerateTokenSuccessfully() {
        String token = jwtService.generateToken("user@example.com");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractEmailFromToken() {
        String token = jwtService.generateToken("user@example.com");

        String email = jwtService.extractEmail(token);

        assertEquals("user@example.com", email);
    }

    @Test
    void shouldExtractClaimsFromToken() {
        String token = jwtService.generateToken("user@example.com");

        var claims = jwtService.extractClaims(token);

        assertEquals("user@example.com", claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void shouldValidateTokenForCorrectEmail() {
        String token = jwtService.generateToken("user@example.com");

        assertTrue(
                jwtService.isTokenValid(token, "user@example.com")
        );
    }

    @Test
    void shouldRejectTokenForDifferentEmail() {
        String token = jwtService.generateToken("user@example.com");

        assertFalse(
                jwtService.isTokenValid(token, "another@example.com")
        );
    }

    @Test
    void shouldRejectTokenSignedWithDifferentSecret() {
        String token = jwtService.generateToken("user@example.com");

        String anotherSecret =
                Base64.getEncoder().encodeToString(
                        "abcdefghijklmnopqrstuvwxyz123456"
                                .getBytes()
                );

        JwtService anotherJwtService =
                new JwtService(anotherSecret, 60000);

        assertFalse(
                anotherJwtService.isTokenValid(
                        token, "user@example.com"
                )
        );
    }

    @Test
    void shouldRejectMalformedToken() {
        assertFalse(
                jwtService.isTokenValid(
                        "this-is-not-a-valid-jwt",
                        "user@example.com"
                )
        );
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtService expiredJwtService =
                new JwtService(SECRET, 0);

        String token =
                expiredJwtService.generateToken("user@example.com");

        assertFalse(
                expiredJwtService.isTokenValid(
                        token, "user@example.com"
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenExtractingEmailFromInvalidToken() {
        assertThrows(
                JwtException.class,
                () -> jwtService.extractEmail("invalid-token")
        );
    }
}
