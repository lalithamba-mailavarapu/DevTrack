
package in.strikes.devTrack.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 2000)
    private String description;

    @Size(max = 50)
    private String primaryLanguage;

    @Size(max = 50)
    private String category;

    @Size(max = 500)
    private String githubRepositoryUrl;

    @Valid
    private List<ProjectTechnologyRequest> technologies = new ArrayList<>();

    public CreateProjectRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrimaryLanguage() {
        return primaryLanguage;
    }

    public void setPrimaryLanguage(String primaryLanguage) {
        this.primaryLanguage = primaryLanguage;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getGithubRepositoryUrl() {
        return githubRepositoryUrl;
    }

    public void setGithubRepositoryUrl(String githubRepositoryUrl) {
        this.githubRepositoryUrl = githubRepositoryUrl;
    }

    public List<ProjectTechnologyRequest> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(List<ProjectTechnologyRequest> technologies) {
        this.technologies = technologies;
    }
}
