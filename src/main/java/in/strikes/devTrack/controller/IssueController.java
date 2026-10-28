
package in.strikes.devTrack.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.strikes.devTrack.dto.AssignIssueRequest;
import in.strikes.devTrack.dto.CreateIssueRequest;
import in.strikes.devTrack.dto.IssueResponse;
import in.strikes.devTrack.dto.UpdateIssueRequest;
import in.strikes.devTrack.entity.Issue;
import in.strikes.devTrack.service.IssueService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects/{projectId}/issues")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateIssueRequest request) {

        IssueResponse response =
                issueService.createIssue(projectId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<Page<IssueResponse>> getProjectIssues(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                issueService.getProjectIssues(projectId, page, size));
    }

    
    @GetMapping("/filter")
    public ResponseEntity<Page<IssueResponse>> filterIssues(
            @PathVariable Long projectId,
            @RequestParam(required = false) Issue.Status status,
            @RequestParam(required = false) Issue.Priority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                issueService.getFilteredProjectIssues(
                        projectId, status, priority, page, size));
    }
    
@PatchMapping("/{issueId}/assignee")
public ResponseEntity<IssueResponse> assignIssue(
        @PathVariable Long projectId,
        @PathVariable Long issueId,
        @Valid @RequestBody AssignIssueRequest request) {

    return ResponseEntity.ok(
            issueService.assignIssue(projectId, issueId, request));
}


@PatchMapping("/{issueId}")
public ResponseEntity<IssueResponse> updateIssue(
        @PathVariable Long projectId,
        @PathVariable Long issueId,
        @Valid @RequestBody UpdateIssueRequest request) {

    return ResponseEntity.ok(
            issueService.updateIssue(projectId, issueId, request));
}


}
