
package in.strikes.devTrack.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import tools.jackson.databind.JsonNode;

@Service
public class GeminiAiService {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiAiService(
            RestClient.Builder restClientBuilder,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model) {

        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public String generateText(String prompt) {

        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException(
                    "Prompt cannot be empty."
            );
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Gemini API key is not configured."
            );
        }

        Map<String, Object> requestBody = Map.of(
                "contents",
                List.of(
                        Map.of(
                                "role", "user",
                                "parts",
                                List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        try {

            JsonNode response = restClient.post()
                    .uri(
                            "https://generativelanguage.googleapis.com/"
                                    + "v1beta/models/{model}:generateContent",
                            model
                    )
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                throw new IllegalStateException(
                        "Gemini returned an empty response."
                );
            }

            JsonNode textNode = response
                    .path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (!textNode.isTextual()
                    || textNode.asText().isBlank()) {

                throw new IllegalStateException(
                        "Gemini did not return a text response."
                );
            }

            return textNode.asText();

        } catch (RestClientResponseException exception) {

            throw new IllegalStateException(
                    "Gemini API request failed. HTTP status: "
                            + exception.getStatusCode().value()
                            + ". Details: "
                            + exception.getResponseBodyAsString(),
                    exception
            );
        }
    }
}
