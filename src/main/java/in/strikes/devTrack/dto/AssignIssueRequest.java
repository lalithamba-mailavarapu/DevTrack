
package in.strikes.devTrack.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AssignIssueRequest {

    @NotNull(message = "Assignee ID is required")
    @Positive(message = "Assignee ID must be positive")
    private Long assigneeId;

    public AssignIssueRequest() {
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }
}
