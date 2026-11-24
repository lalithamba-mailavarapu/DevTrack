
package in.strikes.devTrack.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import in.strikes.devTrack.security.JwtAuthenticationFilter;
import in.strikes.devTrack.service.JwtService;

class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
    }

    @Test
    void shouldCreatePasswordEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        assertNotNull(encoder);

        String password = "StrongPassword123";

        String encodedPassword = encoder.encode(password);

        assertNotEquals(password, encodedPassword);
        assertTrue(encoder.matches(password, encodedPassword));
    }

    @Test
    void shouldCreateJwtAuthenticationFilter() {
        JwtService jwtService = mock(JwtService.class);

        JwtAuthenticationFilter filter =
                securityConfig.jwtAuthenticationFilter(jwtService);

        assertNotNull(filter);
    }

    @Test
    void shouldConfigureAllowedCorsOrigins() {
        CorsConfigurationSource source =
                securityConfig.corsConfigurationSource();

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/projects");

        CorsConfiguration configuration =
                source.getCorsConfiguration(request);

        assertNotNull(configuration);

        assertEquals(
                List.of(
                        "http://localhost:3000",
                        "https://devtrack-frontend-i5ze.onrender.com"
                ),
                configuration.getAllowedOrigins()
        );
    }

    @Test
    void shouldConfigureAllowedCorsMethods() {
        CorsConfiguration configuration =
                getCorsConfiguration();

        assertEquals(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"),
                configuration.getAllowedMethods()
        );
    }

    @Test
    void shouldConfigureAllowedCorsHeaders() {
        CorsConfiguration configuration =
                getCorsConfiguration();

        assertEquals(
                List.of("Authorization", "Content-Type"),
                configuration.getAllowedHeaders()
        );
    }

    @Test
    void shouldDisableCorsCredentials() {
        CorsConfiguration configuration =
                getCorsConfiguration();

        assertFalse(configuration.getAllowCredentials());
    }

    private CorsConfiguration getCorsConfiguration() {
        CorsConfigurationSource source =
                securityConfig.corsConfigurationSource();

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/projects");

        return source.getCorsConfiguration(request);
    }
}
