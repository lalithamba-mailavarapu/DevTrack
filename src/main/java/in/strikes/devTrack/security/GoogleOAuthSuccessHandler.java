
package in.strikes.devTrack.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import in.strikes.devTrack.service.GoogleOAuthService;
import in.strikes.devTrack.service.GitHubOAuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class GoogleOAuthSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final GoogleOAuthService googleOAuthService;
    private final GitHubOAuthService gitHubOAuthService;
    private final String frontendUrl;

    public GoogleOAuthSuccessHandler(
            GoogleOAuthService googleOAuthService,
            GitHubOAuthService gitHubOAuthService,
            @Value("${app.frontend-url:http://localhost:3000}")
            String frontendUrl) {

        this.googleOAuthService = googleOAuthService;
        this.gitHubOAuthService = gitHubOAuthService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            throw new ServletException("Unsupported OAuth authentication.");
        }

        OAuth2User oauthUser = oauthToken.getPrincipal();

        String registrationId =
                oauthToken.getAuthorizedClientRegistrationId();

        String token;

        switch (registrationId) {
            case "google":
                token = googleOAuthService.processGoogleUser(oauthUser);
                break;

            case "github":
                token = gitHubOAuthService.processGitHubUser(oauthUser);
                break;

            default:
                throw new ServletException(
                        "Unsupported OAuth provider: " + registrationId
                );
        }

        String encodedToken =
                URLEncoder.encode(token, StandardCharsets.UTF_8);

        String redirectUrl = frontendUrl
                + "/auth/callback#access_token="
                + encodedToken;

        clearAuthenticationAttributes(request);

        getRedirectStrategy().sendRedirect(
                request,
                response,
                redirectUrl
        );
    }
}
