package in.strikes.devTrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskBreakdownRequest(

        @NotBlank(message = "Project goal is required.")
        @Size(max = 200, message = "Project goal cannot exceed 200 characters.")
        String goal,

        @NotBlank(message = "Project description is required.")
        @Size(max = 5000, message = "Project description cannot exceed 5000 characters.")
        String description

) {
}