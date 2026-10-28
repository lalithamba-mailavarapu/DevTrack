
package in.strikes.devTrack.controller;

import in.strikes.devTrack.dto.AddMemberRequest;
import in.strikes.devTrack.dto.ProjectMembershipResponse;
import in.strikes.devTrack.service.ProjectMembershipService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMembershipController {

    private final ProjectMembershipService membershipService;

    public ProjectMembershipController(
            ProjectMembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping
    public ResponseEntity<ProjectMembershipResponse> addMember(
            @PathVariable Long projectId,
            @Valid @RequestBody AddMemberRequest request) {

        ProjectMembershipResponse response =
                membershipService.addMember(projectId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProjectMembershipResponse>> getMembers(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(
                membershipService.getMembers(projectId));
    }
}
