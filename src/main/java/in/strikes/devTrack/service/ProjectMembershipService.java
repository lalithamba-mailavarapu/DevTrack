
package in.strikes.devTrack.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.strikes.devTrack.dto.AddMemberRequest;
import in.strikes.devTrack.dto.ProjectMembershipResponse;
import in.strikes.devTrack.entity.Project;
import in.strikes.devTrack.entity.ProjectMembership;
import in.strikes.devTrack.entity.User;
import in.strikes.devTrack.exception.ForbiddenException;
import in.strikes.devTrack.exception.ResourceNotFoundException;
import in.strikes.devTrack.repository.ProjectMembershipRepository;
import in.strikes.devTrack.repository.ProjectRepository;
import in.strikes.devTrack.repository.UserRepository;

@Service
public class ProjectMembershipService {

    private final ProjectMembershipRepository membershipRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectMembershipService(
            ProjectMembershipRepository membershipRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository) {
        this.membershipRepository = membershipRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProjectMembershipResponse addMember(
            Long projectId, AddMemberRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = userRepository.findByEmail(
        authentication.getName()
).orElseThrow(() ->
        new ResourceNotFoundException("Authenticated user not found"));

Project project = projectRepository.findById(projectId)
        .orElseThrow(() ->
                new ResourceNotFoundException("Project not found"));

if (!project.getOwner().getId().equals(currentUser.getId())) {
    throw new ForbiddenException(
            "Only the project owner can add members");
}

User newMember = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() ->
                new ResourceNotFoundException(
                        "User with this email does not exist"));

if (membershipRepository.existsByProjectIdAndUserId(
        projectId, newMember.getId())) {
    throw new IllegalArgumentException(
            "User is already a project member");
}

        ProjectMembership membership = new ProjectMembership(
                project, newMember, ProjectMembership.Role.MEMBER);

        membershipRepository.save(membership);

        return new ProjectMembershipResponse(
                newMember.getId(),
                newMember.getUsername(),
                newMember.getEmail(),
                membership.getRole().name()
        );
    }

    @Transactional(readOnly = true)
    public List<ProjectMembershipResponse> getMembers(Long projectId) {

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

        return membershipRepository.findByProjectId(projectId)
                .stream()
                .map(membership -> new ProjectMembershipResponse(
                        membership.getUser().getId(),
                        membership.getUser().getUsername(),
                        membership.getUser().getEmail(),
                        membership.getRole().name()
                ))
                .toList();
    }

    @Transactional
public void removeMember(Long projectId, Long userId) {

    Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

    User currentUser = userRepository.findByEmail(
            authentication.getName()
    ).orElseThrow(() ->
            new ResourceNotFoundException("Authenticated user not found"));

    Project project = projectRepository.findById(projectId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Project not found"));

    // Only the project owner can remove members
    if (!project.getOwner().getId().equals(currentUser.getId())) {
        throw new ForbiddenException(
                "Only the project owner can remove members");
    }

    // Prevent owner from removing themselves
    if (project.getOwner().getId().equals(userId)) {
        throw new ForbiddenException(
                "Project owner cannot be removed");
    }

    ProjectMembership membership = membershipRepository
            .findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Project member not found"));

    membershipRepository.delete(membership);
}
}
