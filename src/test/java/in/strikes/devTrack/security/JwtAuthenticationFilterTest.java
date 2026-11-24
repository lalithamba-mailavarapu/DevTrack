
package in.strikes.devTrack.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.List;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import in.strikes.devTrack.service.JwtService;
import io.jsonwebtoken.MalformedJwtException;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    private JwtAuthenticationFilter jwtFilter;

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtAuthenticationFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest requestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    @Test
    void shouldAuthenticateRequestWithValidToken() throws Exception {
        MockHttpServletRequest request =
                requestWithToken("valid-token");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractEmail("valid-token"))
                .thenReturn("user@example.com");

        when(jwtService.isTokenValid(
                "valid-token", "user@example.com"
        )).thenReturn(true);

        jwtFilter.doFilter(request, response, chain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals("user@example.com", authentication.getPrincipal());
        assertTrue(authentication.isAuthenticated());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_USER")));

        verify(jwtService).extractEmail("valid-token");
        verify(jwtService).isTokenValid(
                "valid-token", "user@example.com"
        );
    }

    @Test
    void shouldContinueWithoutAuthenticationWhenAuthorizationHeaderIsMissing()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        jwtFilter.doFilter(request, response, chain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldContinueWithoutAuthenticationWhenBearerPrefixIsMissing()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader("Authorization", "Basic some-token");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        jwtFilter.doFilter(request, response, chain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsInvalid() throws Exception {
        MockHttpServletRequest request =
                requestWithToken("invalid-token");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractEmail("invalid-token"))
                .thenReturn("user@example.com");

        when(jwtService.isTokenValid(
                "invalid-token", "user@example.com"
        )).thenReturn(false);

        jwtFilter.doFilter(request, response, chain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(jwtService).extractEmail("invalid-token");
        verify(jwtService).isTokenValid(
                "invalid-token", "user@example.com"
        );
    }

    @Test
    void shouldNotAuthenticateWhenTokenParsingThrowsException()
            throws Exception {

        MockHttpServletRequest request =
                requestWithToken("malformed-token");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractEmail("malformed-token"))
                .thenThrow(new MalformedJwtException("Invalid token"));

        assertDoesNotThrow(
                () -> jwtFilter.doFilter(request, response, chain)
        );

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(jwtService).extractEmail("malformed-token");
        verify(jwtService, never()).isTokenValid(anyString(), anyString());
    }

@Test
void shouldNotReplaceExistingAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

    UsernamePasswordAuthenticationToken existingAuth =
            new UsernamePasswordAuthenticationToken(
                    "existing@example.com",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );

    SecurityContextHolder.getContext().setAuthentication(existingAuth);

    request.addHeader("Authorization", "Bearer valid-token");

    when(jwtService.extractEmail("valid-token"))
            .thenReturn("user@example.com");

        jwtFilter.doFilter(request, response, filterChain);

    assertSame(
            existingAuth,
            SecurityContextHolder.getContext().getAuthentication()
    );

    verify(jwtService).extractEmail("valid-token");
    verify(jwtService, never()).isTokenValid(anyString(), anyString());
    verify(filterChain).doFilter(request, response);
}

    @Test
    void shouldContinueRequestWhenTokenValidationThrowsException()
            throws Exception {

        MockHttpServletRequest request =
                requestWithToken("valid-token");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractEmail("valid-token"))
                .thenReturn("user@example.com");

        when(jwtService.isTokenValid(
                "valid-token", "user@example.com"
        )).thenThrow(new IllegalArgumentException("Invalid token"));

        assertDoesNotThrow(
                () -> jwtFilter.doFilter(request, response, chain)
        );

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }
}
