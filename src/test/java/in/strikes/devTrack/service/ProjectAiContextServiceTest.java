
package in.strikes.devTrack.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import in.strikes.devTrack.entity.Project;
import in.strikes.devTrack.entity.User;
import in.strikes.devTrack.exception.ForbiddenException;
import in.strikes.devTrack.exception.ResourceNotFoundException;
import in.strikes.devTrack.repository.IssueRepository;
import in.strikes.devTrack.repository.ProjectMembershipRepository;
import in.strikes.devTrack.repository.ProjectRepository;
import in.strikes.devTrack.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProjectAiContextServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMembershipRepository membershipRepository;

    @Mock
    private IssueRepository issueRepository;

    @InjectMocks
    private ProjectAiContextService projectAiContextService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test@example.com",
                        "password",
                        List.of()
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldBuildProjectContextForMember() {

        User user = mock(User.class);
        Project project = mock(Project.class);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(1L);

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        when(membershipRepository.existsByProjectIdAndUserId(1L, 1L))
                .thenReturn(true);

        when(project.getName()).thenReturn("DevTrack");
        when(project.getDescription())
                .thenReturn("Project management application");
        when(project.getPrimaryLanguage()).thenReturn("Java");
        when(project.getCategory()).thenReturn("Backend");

        when(project.getTechnologies()).thenReturn(List.of());
        when(issueRepository.findByProjectId(1L)).thenReturn(List.of());

        String context = projectAiContextService.buildProjectContext(1L);

        assertDoesNotThrow(() -> {
            if (!context.contains("DevTrack")) {
                throw new AssertionError("Project name missing from context");
            }
        });

        verify(membershipRepository)
                .existsByProjectIdAndUserId(1L, 1L);

        verify(issueRepository).findByProjectId(1L);
    }

    @Test
    void shouldRejectUserWhoIsNotProjectMember() {

        User user = mock(User.class);
        Project project = mock(Project.class);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(1L);

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        when(membershipRepository.existsByProjectIdAndUserId(1L, 1L))
                .thenReturn(false);

        assertThrows(
                ForbiddenException.class,
                () -> projectAiContextService.buildProjectContext(1L)
        );

        verifyNoInteractions(issueRepository);
    }

    @Test
    void shouldThrowExceptionWhenProjectDoesNotExist() {

        User user = mock(User.class);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(projectRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> projectAiContextService.buildProjectContext(1L)
        );

        verifyNoInteractions(membershipRepository, issueRepository);
    }

    @Test
    void shouldRejectUnauthenticatedUser() {

        SecurityContextHolder.clearContext();

        assertThrows(
                ForbiddenException.class,
                () -> projectAiContextService.buildProjectContext(1L)
        );

        verifyNoInteractions(
                userRepository,
                projectRepository,
                membershipRepository,
                issueRepository
        );
    }
}
