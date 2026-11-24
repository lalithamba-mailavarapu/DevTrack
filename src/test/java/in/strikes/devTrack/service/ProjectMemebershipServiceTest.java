
package in.strikes.devTrack.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

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

@ExtendWith(MockitoExtension.class)
class ProjectMembershipServiceTest {

    @Mock
    private ProjectMembershipRepository membershipRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectMembershipService membershipService;

    private User owner;
    private User member;
    private User anotherUser;
    private Project project;

    @BeforeEach
    void setUp() {
        owner = new User("owner", "owner@example.com", "password");
        member = new User("member", "member@example.com", "password");
        anotherUser = new User(
                "another", "another@example.com", "password"
        );

        ReflectionTestUtils.setField(owner, "id", 1L);
        ReflectionTestUtils.setField(member, "id", 2L);
        ReflectionTestUtils.setField(anotherUser, "id", 3L);

        project = new Project(
                "DevTrack", "Project management app", owner
        );
        ReflectionTestUtils.setField(project, "id", 10L);

        authenticateAsMember();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAsMember() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "member@example.com",
                        null,
                        List.of()
                )
        );
    }

    private void authenticateAsOwner() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "owner@example.com",
                        null,
                        List.of()
                )
        );
    }

    private AddMemberRequest addMemberRequest(String email) {
        AddMemberRequest request = new AddMemberRequest();
        request.setEmail(email);
        return request;
    }

    private void mockOwnerAccess() {
        authenticateAsOwner();

        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(owner));

        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(project));
    }

    private void mockMemberAccess() {
        authenticateAsMember();

        when(userRepository.findByEmail("member@example.com"))
                .thenReturn(Optional.of(member));

        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(project));
    }

    // --------------------------------------------------
    // ADD MEMBER TESTS
    // --------------------------------------------------

    @Test
    void shouldAddMemberSuccessfully() {
        mockOwnerAccess();

        when(userRepository.findByEmail("another@example.com"))
                .thenReturn(Optional.of(anotherUser));

        when(membershipRepository.existsByProjectIdAndUserId(10L, 3L))
                .thenReturn(false);

        when(membershipRepository.save(any(ProjectMembership.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProjectMembershipResponse response =
                membershipService.addMember(
                        10L, addMemberRequest("another@example.com")
                );

        assertEquals(3L, response.getUserId());
        assertEquals("another", response.getUsername());
        assertEquals("another@example.com", response.getEmail());
        assertEquals("MEMBER", response.getRole());

        verify(membershipRepository).save(any(ProjectMembership.class));
    }

    @Test
    void shouldRejectAddingMemberWhenCurrentUserIsNotOwner() {
        mockMemberAccess();

        assertThrows(
                ForbiddenException.class,
                () -> membershipService.addMember(
                        10L, addMemberRequest("another@example.com")
                )
        );

        verify(userRepository, never())
                .findByEmail("another@example.com");

        verify(membershipRepository, never()).save(any());
    }

    @Test
    void shouldRejectAddingMemberWhenAuthenticatedUserDoesNotExist() {
        when(userRepository.findByEmail("member@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> membershipService.addMember(
                        10L, addMemberRequest("another@example.com")
                )
        );

        verify(projectRepository, never()).findById(anyLong());
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void shouldRejectAddingMemberWhenProjectDoesNotExist() {
        authenticateAsOwner();

        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(owner));

        when(projectRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> membershipService.addMember(
                        10L, addMemberRequest("another@example.com")
                )
        );

        verify(userRepository, never())
                .findByEmail("another@example.com");

        verify(membershipRepository, never()).save(any());
    }

    @Test
    void shouldRejectAddingMemberWhenTargetUserDoesNotExist() {
        mockOwnerAccess();

        when(userRepository.findByEmail("another@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> membershipService.addMember(
                        10L, addMemberRequest("another@example.com")
                )
        );

        verify(membershipRepository, never()).save(any());
    }

    @Test
    void shouldRejectAddingExistingProjectMember() {
        mockOwnerAccess();

        when(userRepository.findByEmail("member@example.com"))
                .thenReturn(Optional.of(member));

        when(membershipRepository.existsByProjectIdAndUserId(10L, 2L))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> membershipService.addMember(
                        10L, addMemberRequest("member@example.com")
                )
        );

        verify(membershipRepository, never()).save(any());
    }

    // --------------------------------------------------
    // GET MEMBERS TESTS
    // --------------------------------------------------

    @Test
    void shouldReturnMembersForProjectOwner() {
        mockOwnerAccess();

        when(membershipRepository.existsByProjectIdAndUserId(10L, 1L))
                .thenReturn(false);

        ProjectMembership membership = new ProjectMembership(
                project,
                member,
                ProjectMembership.Role.MEMBER
        );

        when(membershipRepository.findByProjectId(10L))
                .thenReturn(List.of(membership));

        List<ProjectMembershipResponse> result =
                membershipService.getMembers(10L);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getUserId());
        assertEquals("member", result.get(0).getUsername());
        assertEquals("member@example.com", result.get(0).getEmail());
        assertEquals("MEMBER", result.get(0).getRole());

        verify(membershipRepository).findByProjectId(10L);
    }

    @Test
    void shouldReturnMembersForExistingProjectMember() {
        mockMemberAccess();

        when(membershipRepository.existsByProjectIdAndUserId(10L, 2L))
                .thenReturn(true);

        ProjectMembership membership = new ProjectMembership(
                project,
                anotherUser,
                ProjectMembership.Role.MEMBER
        );

        when(membershipRepository.findByProjectId(10L))
                .thenReturn(List.of(membership));

        List<ProjectMembershipResponse> result =
                membershipService.getMembers(10L);

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getUserId());
        assertEquals("another", result.get(0).getUsername());
    }

    @Test
    void shouldRejectGettingMembersWithoutProjectAccess() {
        mockMemberAccess();

        when(membershipRepository.existsByProjectIdAndUserId(10L, 2L))
                .thenReturn(false);

        assertThrows(
                ForbiddenException.class,
                () -> membershipService.getMembers(10L)
        );

        verify(membershipRepository, never()).findByProjectId(anyLong());
    }

    @Test
    void shouldRejectGettingMembersWhenProjectDoesNotExist() {
        when(userRepository.findByEmail("member@example.com"))
                .thenReturn(Optional.of(member));

        when(projectRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> membershipService.getMembers(10L)
        );

        verify(membershipRepository, never()).findByProjectId(anyLong());
    }

    @Test
    void shouldRejectGettingMembersWhenAuthenticatedUserDoesNotExist() {
        when(userRepository.findByEmail("member@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> membershipService.getMembers(10L)
        );

        verify(projectRepository, never()).findById(anyLong());
        verify(membershipRepository, never()).findByProjectId(anyLong());
    }

    // --------------------------------------------------
    // REMOVE MEMBER TESTS
    // --------------------------------------------------

    @Test
    void shouldRemoveMemberSuccessfully() {
        mockOwnerAccess();

        ProjectMembership membership = new ProjectMembership(
                project,
                member,
                ProjectMembership.Role.MEMBER
        );

        when(membershipRepository.findByProjectIdAndUserId(10L, 2L))
                .thenReturn(Optional.of(membership));

        membershipService.removeMember(10L, 2L);

        verify(membershipRepository).delete(membership);
    }

    @Test
    void shouldRejectRemovingMemberWhenCurrentUserIsNotOwner() {
        mockMemberAccess();

        assertThrows(
                ForbiddenException.class,
                () -> membershipService.removeMember(10L, 2L)
        );

        verify(membershipRepository, never())
                .findByProjectIdAndUserId(anyLong(), anyLong());

        verify(membershipRepository, never()).delete(any());
    }

    @Test
    void shouldPreventOwnerFromRemovingThemselves() {
        mockOwnerAccess();

        assertThrows(
                ForbiddenException.class,
                () -> membershipService.removeMember(10L, 1L)
        );

        verify(membershipRepository, never())
                .findByProjectIdAndUserId(anyLong(), anyLong());

        verify(membershipRepository, never()).delete(any());
    }

    @Test
    void shouldRejectRemovingUserWhoIsNotAProjectMember() {
        mockOwnerAccess();

        when(membershipRepository.findByProjectIdAndUserId(10L, 3L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> membershipService.removeMember(10L, 3L)
        );

        verify(membershipRepository, never()).delete(any());
    }

    @Test
    void shouldRejectRemovingMemberWhenProjectDoesNotExist() {
        authenticateAsOwner();

        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(owner));

        when(projectRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> membershipService.removeMember(10L, 2L)
        );

        verify(membershipRepository, never()).delete(any());
    }

    @Test
    void shouldRejectRemovingMemberWhenAuthenticatedUserDoesNotExist() {
        when(userRepository.findByEmail("member@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> membershipService.removeMember(10L, 2L)
        );

        verify(projectRepository, never()).findById(anyLong());
        verify(membershipRepository, never()).delete(any());
    }
}
