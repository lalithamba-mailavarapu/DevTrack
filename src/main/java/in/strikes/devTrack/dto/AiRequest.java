package in.strikes.devTrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiRequest(

        @NotBlank(message = "Issue title is required.")
        @Size(max = 200, message = "Issue title cannot exceed 200 characters.")
        String title,

        @NotBlank(message = "Issue description is required.")
        @Size(max = 5000, message = "Issue description cannot exceed 5000 characters.")
        String description

) {
}