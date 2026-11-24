
package in.strikes.devTrack.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import in.strikes.devTrack.service.GoogleOAuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class GoogleOAuthSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final GoogleOAuthService googleOAuthService;
    private final String frontendUrl;

    public GoogleOAuthSuccessHandler(
            GoogleOAuthService googleOAuthService,
            @Value("${app.frontend-url:http://localhost:3000}")
            String frontendUrl) {

        this.googleOAuthService = googleOAuthService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        String token = googleOAuthService.processGoogleUser(
                (org.springframework.security.oauth2.core.user.OAuth2User)
                        authentication.getPrincipal()
        );

        String encodedToken = URLEncoder.encode(
                token,
                StandardCharsets.UTF_8
        );

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
