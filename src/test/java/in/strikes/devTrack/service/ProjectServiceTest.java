
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import in.strikes.devTrack.dto.CreateProjectRequest;
import in.strikes.devTrack.dto.ProjectTechnologyRequest;
import in.strikes.devTrack.entity.Project;
import in.strikes.devTrack.entity.ProjectMembership;
import in.strikes.devTrack.entity.ProjectTechnology;
import in.strikes.devTrack.entity.User;
import in.strikes.devTrack.exception.ForbiddenException;
import in.strikes.devTrack.exception.ResourceNotFoundException;
import in.strikes.devTrack.repository.IssueRepository;
import in.strikes.devTrack.repository.ProjectMembershipRepository;
import in.strikes.devTrack.repository.ProjectRepository;
import in.strikes.devTrack.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMembershipRepository membershipRepository;

    @Mock
    private IssueRepository issueRepository;

    @InjectMocks
    private ProjectService projectService;

    private User owner;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        owner = new User("buddy", "buddy@example.com", "password-hash");
        anotherUser = new User("other", "other@example.com", "password-hash");

        ReflectionTestUtils.setField(owner, "id", 1L);
        ReflectionTestUtils.setField(anotherUser, "id", 2L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "buddy@example.com",
                        null,
                        List.of()
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private CreateProjectRequest createRequest() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("DevTrack");
        request.setDescription("Project management application");
        request.setPrimaryLanguage("Java");
        request.setCategory("Backend");
        request.setGithubRepositoryUrl("https://github.com/example/devtrack");

        ProjectTechnologyRequest java = new ProjectTechnologyRequest();
        java.setTechnologyName("Java");
        java.setTechnologyType("LANGUAGE");

        ProjectTechnologyRequest spring = new ProjectTechnologyRequest();
        spring.setTechnologyName("Spring Boot");
        spring.setTechnologyType("FRAMEWORK");

        request.setTechnologies(List.of(java, spring));

        return request;
    }

    private Project createExistingProject(User projectOwner) {
        Project project = new Project(
                "DevTrack",
                "Project management application",
                projectOwner
        );

        ReflectionTestUtils.setField(project, "id", 10L);

        return project;
    }

    @Test
    void shouldCreateProjectAndAssignOwnerMembership() {
        when(userRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(owner));

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> {
                    Project project = invocation.getArgument(0);
                    ReflectionTestUtils.setField(project, "id", 10L);
                    return project;
                });

        var response = projectService.createProject(createRequest());

        assertEquals(10L, response.getId());
        assertEquals("DevTrack", response.getName());
        assertEquals(1L, response.getOwnerId());
        assertEquals(2, response.getTechnologies().size());

        ArgumentCaptor<ProjectMembership> membershipCaptor =
                ArgumentCaptor.forClass(ProjectMembership.class);

        verify(membershipRepository).save(membershipCaptor.capture());

        ProjectMembership membership = membershipCaptor.getValue();

        assertEquals(ProjectMembership.Role.OWNER, membership.getRole());
        assertEquals(owner, membership.getUser());
        assertEquals(10L, membership.getProject().getId());
    }

    @Test
    void shouldRejectInvalidTechnologyType() {
        when(userRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(owner));

        CreateProjectRequest request = createRequest();
        request.getTechnologies().get(0).setTechnologyType("INVALID_TYPE");

        assertThrows(
                IllegalArgumentException.class,
                () -> projectService.createProject(request)
        );

        verify(projectRepository, never()).save(any());
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void shouldRejectCreationWhenAuthenticatedUserDoesNotExist() {
        when(userRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.createProject(createRequest())
        );

        verify(projectRepository, never()).save(any());
    }

    @Test
    void shouldReturnProjectsOwnedByAuthenticatedUser() {
        Project project = createExistingProject(owner);

        when(userRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(owner));

        when(projectRepository.findByOwnerId(1L))
                .thenReturn(List.of(project));

        var projects = projectService.getMyProjects();

        assertEquals(1, projects.size());
        assertEquals("DevTrack", projects.get(0).getName());
        assertEquals(1L, projects.get(0).getOwnerId());

        verify(projectRepository).findByOwnerId(1L);
    }

    @Test
    void shouldUpdateProjectWhenCurrentUserIsOwner() {
        Project project = createExistingProject(owner);

        when(userRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(owner));

        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(project));

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = projectService.updateProject(10L, createRequest());

        assertEquals("DevTrack", response.getName());
        assertEquals("Java", response.getPrimaryLanguage());
        assertEquals(2, response.getTechnologies().size());

        verify(projectRepository).save(project);
    }

    @Test
    void shouldRejectUpdateWhenCurrentUserIsNotOwner() {
        Project project = createExistingProject(anotherUser);

        when(userRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(owner));

        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(project));

        assertThrows(
                ForbiddenException.class,
                () -> projectService.updateProject(10L, createRequest())
        );

        verify(projectRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWhenProjectDoesNotExist() {
        when(userRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(owner));

        when(projectRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.updateProject(10L, createRequest())
        );

        verify(projectRepository, never()).save(any());
    }

    @Test
    void shouldDeleteProjectAndItsDependentRecordsWhenOwnerDeletesIt() {
        Project project = createExistingProject(owner);

        when(userRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(owner));

        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(project));

        projectService.deleteProject(10L);

        verify(issueRepository).deleteByProjectId(10L);
        verify(membershipRepository).deleteByProjectId(10L);
        verify(projectRepository).delete(project);
    }

    @Test
    void shouldRejectDeletionWhenCurrentUserIsNotOwner() {
        Project project = createExistingProject(anotherUser);

        when(userRepository.findByEmail("buddy@example.com"))
                .thenReturn(Optional.of(owner));

        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(project));

        assertThrows(
                ForbiddenException.class,
                () -> projectService.deleteProject(10L)
        );

        verify(issueRepository, never()).deleteByProjectId(anyLong());
        verify(membershipRepository, never()).deleteByProjectId(anyLong());
        verify(projectRepository, never()).delete(any(Project.class));
    }
}
