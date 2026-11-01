
package in.strikes.devTrack.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private LocalDateTime createdAt;

    private String primaryLanguage;
    private String category;
    private String githubRepositoryUrl;

    private List<ProjectTechnologyResponse> technologies = new ArrayList<>();

    public ProjectResponse(
            Long id,
            String name,
            String description,
            Long ownerId,
            LocalDateTime createdAt,
            String primaryLanguage,
            String category,
            String githubRepositoryUrl) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.primaryLanguage = primaryLanguage;
        this.category = category;
        this.githubRepositoryUrl = githubRepositoryUrl;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getPrimaryLanguage() {
        return primaryLanguage;
    }

    public String getCategory() {
        return category;
    }

    public String getGithubRepositoryUrl() {
        return githubRepositoryUrl;
    }

    public List<ProjectTechnologyResponse> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(List<ProjectTechnologyResponse> technologies) {
        this.technologies = technologies;
    }
}
