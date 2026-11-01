
package in.strikes.devTrack.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.strikes.devTrack.dto.CreateProjectRequest;
import in.strikes.devTrack.dto.ProjectResponse;
import in.strikes.devTrack.dto.ProjectTechnologyRequest;
import in.strikes.devTrack.dto.ProjectTechnologyResponse;
import in.strikes.devTrack.entity.Project;
import in.strikes.devTrack.entity.ProjectTechnology;
import in.strikes.devTrack.entity.ProjectTechnology.TechnologyType;
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
        project.setPrimaryLanguage(request.getPrimaryLanguage());
        project.setCategory(request.getCategory());
        project.setGithubRepositoryUrl(request.getGithubRepositoryUrl());
        project.setOwner(owner);

        if (request.getTechnologies() != null) {
            for (ProjectTechnologyRequest technologyRequest : request.getTechnologies()) {

                TechnologyType type;

                try {
                    type = TechnologyType.valueOf(
                            technologyRequest.getTechnologyType().trim().toUpperCase());
                } catch (IllegalArgumentException | NullPointerException exception) {
                    throw new IllegalArgumentException(
                            "Invalid technology type: " +
                            technologyRequest.getTechnologyType());
                }

                ProjectTechnology technology = new ProjectTechnology(
                        technologyRequest.getTechnologyName().trim(),
                        type,
                        project
                );

                project.addTechnology(technology);
            }
        }

        Project savedProject = projectRepository.save(project);

        return mapToResponse(savedProject);
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
                .map(this::mapToResponse)
                .toList();
    }

    private ProjectResponse mapToResponse(Project project) {

        ProjectResponse response = new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwner().getId(),
                project.getCreatedAt(),
                project.getPrimaryLanguage(),
                project.getCategory(),
                project.getGithubRepositoryUrl()
        );

        List<ProjectTechnologyResponse> technologies =
                project.getTechnologies()
                        .stream()
                        .map(technology -> new ProjectTechnologyResponse(
                                technology.getId(),
                                technology.getTechnologyName(),
                                technology.getTechnologyType()
                        ))
                        .toList();

        response.setTechnologies(technologies);

        return response;
    }
}
