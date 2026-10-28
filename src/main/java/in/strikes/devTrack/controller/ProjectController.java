
package in.strikes.devTrack.controller;

import in.strikes.devTrack.dto.CreateProjectRequest;
import in.strikes.devTrack.dto.ProjectResponse;
import in.strikes.devTrack.service.ProjectService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request) {

        ProjectResponse response =
                projectService.createProject(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getMyProjects() {
        return ResponseEntity.ok(projectService.getMyProjects());
    }

}
