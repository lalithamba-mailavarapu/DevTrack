
package in.strikes.devTrack.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ResendEmailService {

    private final RestClient restClient;
    private final String fromEmail;

    public ResendEmailService(
            RestClient.Builder builder,
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from-email}") String fromEmail) {

        this.restClient = builder
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        this.fromEmail = fromEmail;
    }

    public void sendEmail(String to, String subject, String html) {

        Map<String, Object> request = Map.of(
                "from", fromEmail,
                "to", List.of(to),
                "subject", subject,
                "html", html
        );

        restClient.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
