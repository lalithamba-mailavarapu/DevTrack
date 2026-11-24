
package in.strikes.devTrack.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.time.Instant;

@ExtendWith(MockitoExtension.class)
class GitHubOAuth2UserServiceTest {

    @Mock
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;

    private MockRestServiceServer server;
    private GitHubOAuth2UserService service;
    private OAuth2UserRequest userRequest;
    private OAuth2User githubUser;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();

        server = MockRestServiceServer.bindTo(builder).build();

        service = new GitHubOAuth2UserService(delegate, builder);

        ClientRegistration registration =
                ClientRegistration.withRegistrationId("github")
                        .clientId("test-client")
                        .clientSecret("test-secret")
                        .authorizationGrantType(
                                AuthorizationGrantType.AUTHORIZATION_CODE
                        )
                        .redirectUri("http://localhost/login/oauth2/code/github")
                        .scope("read:user", "user:email")
                        .authorizationUri("https://github.com/login/oauth/authorize")
                        .tokenUri("https://github.com/login/oauth/access_token")
                        .userInfoUri("https://api.github.com/user")
                        .userNameAttributeName("id")
                        .clientName("GitHub")
                        .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "mock-access-token",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        userRequest = new OAuth2UserRequest(registration, accessToken);

        githubUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "id", 12345,
                        "login", "buddy"
                ),
                "id"
        );

        when(delegate.loadUser(userRequest)).thenReturn(githubUser);
    }

    @Test
    void shouldReturnUserWithPrimaryVerifiedEmail() {
        server.expect(requestTo("https://api.github.com/user/emails"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(
                        "Authorization",
                        "Bearer mock-access-token"
                ))
                .andRespond(withSuccess(
                        """
                        [
                          {
                            "email": "secondary@example.com",
                            "primary": false,
                            "verified": true
                          },
                          {
                            "email": "buddy@example.com",
                            "primary": true,
                            "verified": true
                          }
                        ]
                        """,
                        MediaType.APPLICATION_JSON
                ));

        OAuth2User result = service.loadUser(userRequest);

        assertEquals("buddy@example.com", result.getAttribute("email"));
        assertEquals(12345, ((Number) result.getAttribute("id")).intValue());
        assertEquals("buddy", result.getAttribute("login"));

        server.verify();
    }

    @Test
    void shouldRejectWhenNoPrimaryVerifiedEmailExists() {
        server.expect(requestTo("https://api.github.com/user/emails"))
                .andRespond(withSuccess(
                        """
                        [
                          {
                            "email": "buddy@example.com",
                            "primary": true,
                            "verified": false
                          }
                        ]
                        """,
                        MediaType.APPLICATION_JSON
                ));

        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> service.loadUser(userRequest)
        );

        assertEquals(
                "no_verified_github_email",
                exception.getError().getErrorCode()
        );

        server.verify();
    }

    @Test
    void shouldRejectWhenEmailListIsEmpty() {
        server.expect(requestTo("https://api.github.com/user/emails"))
                .andRespond(withSuccess(
                        "[]",
                        MediaType.APPLICATION_JSON
                ));

        assertThrows(
                OAuth2AuthenticationException.class,
                () -> service.loadUser(userRequest)
        );

        server.verify();
    }

    @Test
    void shouldRejectWhenGitHubEmailApiFails() {
        server.expect(requestTo("https://api.github.com/user/emails"))
                .andRespond(withServerError());

        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> service.loadUser(userRequest)
        );

        assertEquals(
                "github_email_lookup_failed",
                exception.getError().getErrorCode()
        );

        server.verify();
    }
}
