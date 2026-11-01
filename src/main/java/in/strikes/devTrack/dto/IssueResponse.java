
package in.strikes.devTrack.dto;

import java.time.LocalDateTime;

public class IssueResponse {

    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String label;
    private Long projectId;
    private Long reporterId;
    private Long assigneeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public IssueResponse(
            Long id,
            String title,
            String description,
            String status,
            String priority,
            Long projectId,
            Long reporterId,
            Long assigneeId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.projectId = projectId;
        this.reporterId = reporterId;
        this.assigneeId = assigneeId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getPriority() {
        return priority;
    }

    public String getLabel() {
    return label;
}

public void setLabel(String label) {
    this.label = label;
}

    public Long getProjectId() {
        return projectId;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
