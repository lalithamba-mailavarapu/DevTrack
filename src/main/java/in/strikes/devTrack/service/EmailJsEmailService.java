
package in.strikes.devTrack.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class EmailJsEmailService {

    private final RestClient restClient;
    private final String serviceId;
    private final String templateId;
    private final String publicKey;
    private final String privateKey;

    public EmailJsEmailService(
            @Value("${emailjs.service-id}") String serviceId,
            @Value("${emailjs.template-id}") String templateId,
            @Value("${emailjs.public-key}") String publicKey,
            @Value("${emailjs.private-key}") String privateKey) {

        this.restClient = RestClient.builder()
                .baseUrl("https://api.emailjs.com")
                .build();

        this.serviceId = serviceId;
        this.templateId = templateId;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public void sendVerificationEmail(
            String email,
            String username,
            String passcode) {

        Map<String, Object> templateParams = Map.of(
                "email", email,
                "username", username,
                "passcode", passcode
        );

        Map<String, Object> request = Map.of(
                "service_id", serviceId,
                "template_id", templateId,
                "user_id", publicKey,
                "accessToken", privateKey,
                "template_params", templateParams
        );

        restClient.post()
                .uri("/api/v1.0/email/send")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
