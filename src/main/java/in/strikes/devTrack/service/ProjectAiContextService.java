
package in.strikes.devTrack.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.strikes.devTrack.entity.Issue;
import in.strikes.devTrack.entity.Project;
import in.strikes.devTrack.entity.ProjectTechnology;
import in.strikes.devTrack.entity.User;
import in.strikes.devTrack.exception.ForbiddenException;
import in.strikes.devTrack.exception.ResourceNotFoundException;
import in.strikes.devTrack.repository.IssueRepository;
import in.strikes.devTrack.repository.ProjectMembershipRepository;
import in.strikes.devTrack.repository.ProjectRepository;
import in.strikes.devTrack.repository.UserRepository;

@Service
public class ProjectAiContextService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMembershipRepository membershipRepository;
    private final IssueRepository issueRepository;

    public ProjectAiContextService(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            ProjectMembershipRepository membershipRepository,
            IssueRepository issueRepository) {

        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.issueRepository = issueRepository;
    }

    @Transactional(readOnly = true)
    public String buildProjectContext(Long projectId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("Authentication is required.");
        }

        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found."));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found."));

        boolean isMember =
                membershipRepository.existsByProjectIdAndUserId(
                        projectId, currentUser.getId());

        if (!isMember) {
            throw new ForbiddenException(
                    "You do not have access to this project.");
        }

        StringBuilder context = new StringBuilder();

        context.append("PROJECT DETAILS\n");
        context.append("Name: ").append(project.getName()).append("\n");
        context.append("Description: ")
                .append(project.getDescription()).append("\n");
        context.append("Primary Language: ")
                .append(project.getPrimaryLanguage()).append("\n");
        context.append("Category: ")
                .append(project.getCategory()).append("\n");

        context.append("\nTECHNOLOGIES\n");

        for (ProjectTechnology technology : project.getTechnologies()) {
            context.append("- ")
                    .append(technology.getTechnologyName())
                    .append(" (")
                    .append(technology.getTechnologyType())
                    .append(")\n");
        }

        List<Issue> issues = issueRepository.findByProjectId(projectId);

        context.append("\nPROJECT ISSUES\n");

        if (issues.isEmpty()) {
            context.append("No issues have been created yet.\n");
        } else {
            context.append("Showing up to 20 project issues:\n");

            issues.stream()
                    .limit(20)
                    .forEach(issue -> {
                        context.append("\nTitle: ")
                                .append(issue.getTitle()).append("\n");

                        context.append("Description: ")
                                .append(issue.getDescription()).append("\n");

                        context.append("Status: ")
                                .append(issue.getStatus()).append("\n");

                        context.append("Priority: ")
                                .append(issue.getPriority()).append("\n");
                    });
        }

        return context.toString();
    }
}
