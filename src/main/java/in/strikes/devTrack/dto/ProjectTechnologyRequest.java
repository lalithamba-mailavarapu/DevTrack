
package in.strikes.devTrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProjectTechnologyRequest {

    @NotBlank(message = "Technology name is required")
    @Size(max = 100, message = "Technology name cannot exceed 100 characters")
    private String technologyName;

    @NotBlank(message = "Technology type is required")
    private String technologyType;

    public ProjectTechnologyRequest() {
    }

    public String getTechnologyName() {
        return technologyName;
    }

    public void setTechnologyName(String technologyName) {
        this.technologyName = technologyName;
    }

    public String getTechnologyType() {
        return technologyType;
    }

    public void setTechnologyType(String technologyType) {
        this.technologyType = technologyType;
    }
}
