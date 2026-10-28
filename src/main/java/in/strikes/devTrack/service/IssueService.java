
package in.strikes.devTrack.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.strikes.devTrack.dto.AssignIssueRequest;
import in.strikes.devTrack.dto.CreateIssueRequest;
import in.strikes.devTrack.dto.IssueResponse;
import in.strikes.devTrack.dto.UpdateIssueRequest;
import in.strikes.devTrack.entity.Issue;
import in.strikes.devTrack.entity.Project;
import in.strikes.devTrack.entity.User;
import in.strikes.devTrack.exception.ForbiddenException;
import in.strikes.devTrack.exception.ResourceNotFoundException;
import in.strikes.devTrack.repository.IssueRepository;
import in.strikes.devTrack.repository.ProjectMembershipRepository;
import in.strikes.devTrack.repository.ProjectRepository;
import in.strikes.devTrack.repository.UserRepository;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public IssueService(
            IssueRepository issueRepository,
            ProjectRepository projectRepository,
            ProjectMembershipRepository membershipRepository,
            UserRepository userRepository) {

        this.issueRepository = issueRepository;
        this.projectRepository = projectRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public IssueResponse createIssue(
            Long projectId,
            CreateIssueRequest request) {

        // 1. Identify the authenticated user.
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Authentication required");
        }

        String email = authentication.getName();

        User reporter = userRepository.findByEmail(email)
                .orElseThrow(() ->
                         new ResourceNotFoundException("Authenticated user not found"));

        // 2. Find the project.
        Project project = projectRepository.findById(projectId)
        .orElseThrow(() ->
                new ResourceNotFoundException("Project not found"));

        // 3. Check project access.
        boolean isOwner = project.getOwner().getId()
                .equals(reporter.getId());

        boolean isMember = membershipRepository
                .existsByProjectIdAndUserId(projectId, reporter.getId());

        if (!isOwner && !isMember) {
    throw new ForbiddenException(
            "You do not have access to this project");
}

        // 4. Create the issue.
        Issue issue = new Issue();
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setProject(project);
        issue.setReporter(reporter);
        issue.setStatus(Issue.Status.OPEN);
        issue.setPriority(Issue.Priority.MEDIUM);

        // 5. Save the issue.
        Issue savedIssue = issueRepository.save(issue);

        // 6. Return a DTO rather than exposing the entity.
        return new IssueResponse(
                savedIssue.getId(),
                savedIssue.getTitle(),
                savedIssue.getDescription(),
                savedIssue.getStatus().name(),
                savedIssue.getPriority().name(),
                savedIssue.getProject().getId(),
                savedIssue.getReporter().getId(),
                savedIssue.getAssignee() == null
                        ? null
                        : savedIssue.getAssignee().getId(),
                savedIssue.getCreatedAt(),
                savedIssue.getUpdatedAt()
        );
    }

    
    @Transactional(readOnly = true)
    public Page<IssueResponse> getProjectIssues(
            Long projectId, int page, int size) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(() ->
                 new ResourceNotFoundException("Authenticated user not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                         new ResourceNotFoundException("Project not found"));

        boolean isOwner = project.getOwner().getId()
                .equals(currentUser.getId());

        boolean isMember = membershipRepository
                .existsByProjectIdAndUserId(projectId, currentUser.getId());

        if (!isOwner && !isMember) {
             throw new ForbiddenException(
            "You do not have access to this project");
        }

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page must be non-negative and size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(
                page, size, Sort.by("createdAt").descending());

        return issueRepository.findByProjectId(projectId, pageable)
                .map(issue -> new IssueResponse(
                        issue.getId(),
                        issue.getTitle(),
                        issue.getDescription(),
                        issue.getStatus().name(),
                        issue.getPriority().name(),
                        issue.getProject().getId(),
                        issue.getReporter().getId(),
                        issue.getAssignee() == null
                                ? null : issue.getAssignee().getId(),
                        issue.getCreatedAt(),
                        issue.getUpdatedAt()
                ));
    }
    
    @Transactional(readOnly = true)
    public Page<IssueResponse> getFilteredProjectIssues(
            Long projectId,
            Issue.Status status,
            Issue.Priority priority,
            int page,
            int size) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(() ->
                new ResourceNotFoundException("Authenticated user not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        boolean isOwner = project.getOwner().getId()
                .equals(currentUser.getId());

        boolean isMember = membershipRepository
                .existsByProjectIdAndUserId(projectId, currentUser.getId());

        if (!isOwner && !isMember) {
            throw new ForbiddenException(
                    "You do not have access to this project");
        }

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page must be non-negative and size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(
                page, size, Sort.by("createdAt").descending());

        Page<Issue> issues;

        if (status != null && priority != null) {
            issues = issueRepository
                    .findByProjectIdAndStatusAndPriority(
                            projectId, status, priority, pageable);
        } else if (status != null) {
            issues = issueRepository
                    .findByProjectIdAndStatus(projectId, status, pageable);
        } else if (priority != null) {
            issues = issueRepository
                    .findByProjectIdAndPriority(projectId, priority, pageable);
        } else {
            issues = issueRepository.findByProjectId(projectId, pageable);
        }

        return issues.map(issue -> new IssueResponse(
                issue.getId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getStatus().name(),
                issue.getPriority().name(),
                issue.getProject().getId(),
                issue.getReporter().getId(),
                issue.getAssignee() == null
                        ? null : issue.getAssignee().getId(),
                issue.getCreatedAt(),
                issue.getUpdatedAt()
        ));
    }
    
@Transactional
public IssueResponse assignIssue(
        Long projectId,
        Long issueId,
        AssignIssueRequest request) {

    Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

    User currentUser = userRepository.findByEmail(
            authentication.getName()
    ).orElseThrow(() ->
            new ResourceNotFoundException("Authenticated user not found"));

    Project project = projectRepository.findById(projectId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Project not found"));

    boolean isOwner = project.getOwner().getId()
            .equals(currentUser.getId());

    boolean isMember = membershipRepository
            .existsByProjectIdAndUserId(projectId, currentUser.getId());

    if (!isOwner && !isMember) {
        throw new ForbiddenException(
                "You do not have access to this project");
    }

    Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Issue not found"));

    if (!issue.getProject().getId().equals(projectId)) {
        throw new ForbiddenException(
                "Issue does not belong to this project");
    }

    User assignee = userRepository.findById(request.getAssigneeId())
            .orElseThrow(() ->
                    new ResourceNotFoundException("Assignee not found"));

    boolean assigneeIsOwner = project.getOwner().getId()
            .equals(assignee.getId());

    boolean assigneeIsMember = membershipRepository
            .existsByProjectIdAndUserId(projectId, assignee.getId());

    if (!assigneeIsOwner && !assigneeIsMember) {
        throw new ForbiddenException(
                "Assignee must belong to this project");
    }

    issue.setAssignee(assignee);

    Issue saved = issueRepository.save(issue);

    return new IssueResponse(
            saved.getId(),
            saved.getTitle(),
            saved.getDescription(),
            saved.getStatus().name(),
            saved.getPriority().name(),
            saved.getProject().getId(),
            saved.getReporter().getId(),
            saved.getAssignee().getId(),
            saved.getCreatedAt(),
            saved.getUpdatedAt()
    );
}


@Transactional
public IssueResponse updateIssue(
        Long projectId,
        Long issueId,
        UpdateIssueRequest request) {

    Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

    User currentUser = userRepository.findByEmail(
            authentication.getName()
    ).orElseThrow(() ->
            new ResourceNotFoundException("Authenticated user not found"));

    Project project = projectRepository.findById(projectId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Project not found"));

    boolean isOwner = project.getOwner().getId()
            .equals(currentUser.getId());

    boolean isMember = membershipRepository
            .existsByProjectIdAndUserId(projectId, currentUser.getId());

    if (!isOwner && !isMember) {
        throw new ForbiddenException(
                "You do not have access to this project");
    }

    Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Issue not found"));

    if (!issue.getProject().getId().equals(projectId)) {
        throw new ForbiddenException(
                "Issue does not belong to this project");
    }

    if (request.getTitle() != null) {
        if (request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        issue.setTitle(request.getTitle());
    }

    if (request.getDescription() != null) {
        issue.setDescription(request.getDescription());
    }

    if (request.getStatus() != null) {
        try {
            issue.setStatus(
                    Issue.Status.valueOf(request.getStatus().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status. Use OPEN, IN_PROGRESS, or CLOSED");
        }
    }

    if (request.getPriority() != null) {
        try {
            issue.setPriority(
                    Issue.Priority.valueOf(request.getPriority().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid priority. Use LOW, MEDIUM, or HIGH");
        }
    }

    Issue saved = issueRepository.save(issue);

    return new IssueResponse(
            saved.getId(),
            saved.getTitle(),
            saved.getDescription(),
            saved.getStatus().name(),
            saved.getPriority().name(),
            saved.getProject().getId(),
            saved.getReporter().getId(),
            saved.getAssignee() == null
                    ? null : saved.getAssignee().getId(),
            saved.getCreatedAt(),
            saved.getUpdatedAt()
    );
}

}
