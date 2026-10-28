
package in.strikes.devTrack.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.strikes.devTrack.dto.CreateProjectRequest;
import in.strikes.devTrack.dto.ProjectResponse;
import in.strikes.devTrack.entity.Project;
import in.strikes.devTrack.entity.User;
import in.strikes.devTrack.exception.ResourceNotFoundException;
import in.strikes.devTrack.repository.ProjectRepository;
import in.strikes.devTrack.repository.UserRepository;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() ->
                         new ResourceNotFoundException("Authenticated user not found"));

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);

        Project savedProject = projectRepository.save(project);

        return new ProjectResponse(
                savedProject.getId(),
                savedProject.getName(),
                savedProject.getDescription(),
                owner.getId(),
                savedProject.getCreatedAt()
        );
    }

    
    @Transactional(readOnly = true)
    public List<ProjectResponse> getMyProjects() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() ->
                         new ResourceNotFoundException("Authenticated user not found"));

        return projectRepository.findByOwnerId(owner.getId())
                .stream()
                .map(project -> new ProjectResponse(
                        project.getId(),
                        project.getName(),
                        project.getDescription(),
                        owner.getId(),
                        project.getCreatedAt()
                ))
                .toList();
    }

}
