
package in.strikes.devTrack.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class GitHubOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate =
            new DefaultOAuth2UserService();

    private final RestClient restClient = RestClient.create();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User githubUser = delegate.loadUser(userRequest);

        List<Map<String, Object>> emails;

        try {
            emails = restClient.get()
                    .uri("https://api.github.com/user/emails")
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + userRequest.getAccessToken().getTokenValue()
                    )
                    .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                    .header(HttpHeaders.USER_AGENT, "DevTrack")
                    .retrieve()
                    .body(new ParameterizedTypeReference<
                            List<Map<String, Object>>>() {});
        } catch (RestClientException exception) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("github_email_lookup_failed"),
                    "Unable to retrieve your GitHub email address.",
                    exception
            );
        }

        String verifiedEmail = null;

        if (emails != null) {
            for (Map<String, Object> emailEntry : emails) {
                boolean primary =
                        Boolean.TRUE.equals(emailEntry.get("primary"));

                boolean verified =
                        Boolean.TRUE.equals(emailEntry.get("verified"));

                Object emailValue = emailEntry.get("email");

                if (primary && verified && emailValue instanceof String email
                        && !email.isBlank()) {
                    verifiedEmail = email;
                    break;
                }
            }
        }

        if (verifiedEmail == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("no_verified_github_email"),
                    "Your GitHub account does not have a primary verified email address."
            );
        }

        Map<String, Object> attributes =
                new LinkedHashMap<>(githubUser.getAttributes());

        attributes.put("email", verifiedEmail);

        return new DefaultOAuth2User(
                githubUser.getAuthorities(),
                attributes,
                "id"
        );
    }
}
