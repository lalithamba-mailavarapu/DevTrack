
package in.strikes.devTrack.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

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

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMembershipRepository membershipRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private IssueService issueService;

    private User owner;
    private User reporter;
    private Project project;

    @BeforeEach
    void setUp() {
        owner = new User("owner", "owner@example.com", "password-hash");
        reporter = new User("reporter", "reporter@example.com", "password-hash");

        ReflectionTestUtils.setField(owner, "id", 1L);
        ReflectionTestUtils.setField(reporter, "id", 2L);

        project = new Project("DevTrack", "Project management app", owner);
        ReflectionTestUtils.setField(project, "id", 10L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "reporter@example.com",
                        null,
                        List.of()
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private CreateIssueRequest createRequest() {
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("Fix login bug");
        request.setDescription("Login fails for some users");
        request.setLabel("BUG");
        return request;
    }

    private void mockProjectAccess(boolean isMember) {
        when(userRepository.findByEmail("reporter@example.com"))
                .thenReturn(Optional.of(reporter));

        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(project));

        when(membershipRepository.existsByProjectIdAndUserId(10L, 2L))
                .thenReturn(isMember);
    }

    private Issue createIssue() {
        Issue issue = new Issue();
        issue.setTitle("Fix login bug");
        issue.setDescription("Login failure");
        issue.setProject(project);
        issue.setReporter(reporter);
        issue.setStatus(Issue.Status.OPEN);
        issue.setPriority(Issue.Priority.MEDIUM);

        ReflectionTestUtils.setField(issue, "id", 100L);
        ReflectionTestUtils.setField(
                issue, "createdAt", LocalDateTime.now()
        );
        ReflectionTestUtils.setField(
                issue, "updatedAt", LocalDateTime.now()
        );

        return issue;
    }

    // --------------------------------------------------
    // ISSUE CREATION TESTS
    // --------------------------------------------------

    @Test
    void shouldCreateIssueForProjectMember() {
        mockProjectAccess(true);

        when(issueRepository.save(any(Issue.class)))
                .thenAnswer(invocation -> {
                    Issue issue = invocation.getArgument(0);
                    ReflectionTestUtils.setField(issue, "id", 100L);
                    ReflectionTestUtils.setField(
                            issue, "createdAt", LocalDateTime.now()
                    );
                    ReflectionTestUtils.setField(
                            issue, "updatedAt", LocalDateTime.now()
                    );
                    return issue;
                });

        IssueResponse response =
                issueService.createIssue(10L, createRequest());

        assertEquals(100L, response.getId());
        assertEquals("Fix login bug", response.getTitle());
        assertEquals("Login fails for some users", response.getDescription());
        assertEquals("OPEN", response.getStatus());
        assertEquals("MEDIUM", response.getPriority());
        assertEquals("BUG", response.getLabel());
        assertEquals(10L, response.getProjectId());
        assertEquals(2L, response.getReporterId());
        assertNull(response.getAssigneeId());

        verify(issueRepository).save(any(Issue.class));
    }

    @Test
    void shouldRejectIssueCreationForUserWithoutProjectAccess() {
        mockProjectAccess(false);

        assertThrows(
                ForbiddenException.class,
                () -> issueService.createIssue(10L, createRequest())
        );

        verify(issueRepository, never()).save(any());
    }

    @Test
    void shouldAllowProjectOwnerToCreateIssue() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "owner@example.com",
                        null,
                        List.of()
                )
        );

        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(owner));

        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(project));

        when(membershipRepository.existsByProjectIdAndUserId(10L, 1L))
                .thenReturn(false);

        when(issueRepository.save(any(Issue.class)))
                .thenAnswer(invocation -> {
                    Issue issue = invocation.getArgument(0);
                    ReflectionTestUtils.setField(issue, "id", 101L);
                    ReflectionTestUtils.setField(
                            issue, "createdAt", LocalDateTime.now()
                    );
                    ReflectionTestUtils.setField(
                            issue, "updatedAt", LocalDateTime.now()
                    );
                    return issue;
                });

        IssueResponse response =
                issueService.createIssue(10L, createRequest());

        assertEquals(101L, response.getId());
        assertEquals(1L, response.getReporterId());

        verify(issueRepository).save(any(Issue.class));
    }

    @Test
    void shouldRejectIssueCreationWhenProjectDoesNotExist() {
        when(userRepository.findByEmail("reporter@example.com"))
                .thenReturn(Optional.of(reporter));

        when(projectRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> issueService.createIssue(10L, createRequest())
        );

        verify(issueRepository, never()).save(any());
    }

    @Test
    void shouldRejectIssueCreationWhenAuthenticatedUserDoesNotExist() {
        when(userRepository.findByEmail("reporter@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> issueService.createIssue(10L, createRequest())
        );

        verify(projectRepository, never()).findById(anyLong());
        verify(issueRepository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidIssueLabel() {
        mockProjectAccess(true);

        CreateIssueRequest request = createRequest();
        request.setLabel("INVALID_LABEL");

        assertThrows(
                IllegalArgumentException.class,
                () -> issueService.createIssue(10L, request)
        );

        verify(issueRepository, never()).save(any());
    }

    @Test
    void shouldCreateIssueWithoutOptionalLabel() {
        mockProjectAccess(true);

        CreateIssueRequest request = createRequest();
        request.setLabel(null);

        when(issueRepository.save(any(Issue.class)))
                .thenAnswer(invocation -> {
                    Issue issue = invocation.getArgument(0);
                    ReflectionTestUtils.setField(issue, "id", 102L);
                    ReflectionTestUtils.setField(
                            issue, "createdAt", LocalDateTime.now()
                    );
                    ReflectionTestUtils.setField(
                            issue, "updatedAt", LocalDateTime.now()
                    );
                    return issue;
                });

        IssueResponse response =
                issueService.createIssue(10L, request);

        assertEquals(102L, response.getId());
        assertNull(response.getLabel());
    }

    // --------------------------------------------------
    // PAGINATION AND FILTERING TESTS
    // --------------------------------------------------

    @Test
    void shouldReturnPaginatedIssuesForProjectMember() {
        mockProjectAccess(true);

        Issue issue = createIssue();

        Pageable pageable = PageRequest.of(
                0, 10, Sort.by("createdAt").descending()
        );

        when(issueRepository.findByProjectId(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(issue), pageable, 1));

        Page<IssueResponse> result =
                issueService.getProjectIssues(10L, 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("Fix login bug", result.getContent().get(0).getTitle());

        verify(issueRepository).findByProjectId(
                eq(10L),
                argThat(p -> p.getPageNumber() == 0
                        && p.getPageSize() == 10
                        && p.getSort().getOrderFor("createdAt").isDescending())
        );
    }

    @Test
    void shouldRejectInvalidPaginationParameters() {
        mockProjectAccess(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> issueService.getProjectIssues(10L, -1, 10)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> issueService.getProjectIssues(10L, 0, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> issueService.getProjectIssues(10L, 0, 101)
        );

        verify(issueRepository, never())
                .findByProjectId(anyLong(), any(Pageable.class));
    }

    @Test
    void shouldFilterIssuesByStatus() {
        mockProjectAccess(true);

        Pageable pageable = PageRequest.of(
                0, 10, Sort.by("createdAt").descending()
        );

        when(issueRepository.findByProjectIdAndStatus(
                eq(10L),
                eq(Issue.Status.OPEN),
                any(Pageable.class)
        )).thenReturn(Page.empty(pageable));

        Page<IssueResponse> result =
                issueService.getFilteredProjectIssues(
                        10L, Issue.Status.OPEN, null, 0, 10
                );

        assertNotNull(result);

        verify(issueRepository).findByProjectIdAndStatus(
                eq(10L), eq(Issue.Status.OPEN), any(Pageable.class)
        );

        verify(issueRepository, never()).findByProjectIdAndPriority(
                anyLong(), any(), any(Pageable.class)
        );
    }

    @Test
    void shouldFilterIssuesByPriority() {
        mockProjectAccess(true);

        Pageable pageable = PageRequest.of(
                0, 10, Sort.by("createdAt").descending()
        );

        when(issueRepository.findByProjectIdAndPriority(
                eq(10L),
                eq(Issue.Priority.HIGH),
                any(Pageable.class)
        )).thenReturn(Page.empty(pageable));

        Page<IssueResponse> result =
                issueService.getFilteredProjectIssues(
                        10L, null, Issue.Priority.HIGH, 0, 10
                );

        assertNotNull(result);

        verify(issueRepository).findByProjectIdAndPriority(
                eq(10L), eq(Issue.Priority.HIGH), any(Pageable.class)
        );
    }

    @Test
    void shouldFilterIssuesByBothStatusAndPriority() {
        mockProjectAccess(true);

        Pageable pageable = PageRequest.of(
                0, 10, Sort.by("createdAt").descending()
        );

        when(issueRepository.findByProjectIdAndStatusAndPriority(
                eq(10L),
                eq(Issue.Status.OPEN),
                eq(Issue.Priority.HIGH),
                any(Pageable.class)
        )).thenReturn(Page.empty(pageable));

        Page<IssueResponse> result =
                issueService.getFilteredProjectIssues(
                        10L,
                        Issue.Status.OPEN,
                        Issue.Priority.HIGH,
                        0,
                        10
                );

        assertNotNull(result);

        verify(issueRepository).findByProjectIdAndStatusAndPriority(
                eq(10L),
                eq(Issue.Status.OPEN),
                eq(Issue.Priority.HIGH),
                any(Pageable.class)
        );
    }

    @Test
    void shouldReturnAllIssuesWhenNoFiltersAreProvided() {
        mockProjectAccess(true);

        Pageable pageable = PageRequest.of(
                0, 10, Sort.by("createdAt").descending()
        );

        when(issueRepository.findByProjectId(
                eq(10L), any(Pageable.class)
        )).thenReturn(Page.empty(pageable));

        Page<IssueResponse> result =
                issueService.getFilteredProjectIssues(
                        10L, null, null, 0, 10
                );

        assertNotNull(result);

        verify(issueRepository).findByProjectId(
                eq(10L), any(Pageable.class)
        );
    }

    @Test
    void shouldRejectFilteringWithInvalidPagination() {
        mockProjectAccess(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> issueService.getFilteredProjectIssues(
                        10L, Issue.Status.OPEN, null, -1, 10
                )
        );

        verify(issueRepository, never()).findByProjectIdAndStatus(
                anyLong(), any(), any(Pageable.class)
        );
    }

    // --------------------------------------------------
    // ISSUE ASSIGNMENT TESTS
    // --------------------------------------------------

    @Test
    void shouldAssignIssueToProjectOwner() {
        mockProjectAccess(true);

        Issue issue = createIssue();

        when(issueRepository.findById(100L))
                .thenReturn(Optional.of(issue));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));

        when(issueRepository.save(any(Issue.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AssignIssueRequest request = new AssignIssueRequest();
        request.setAssigneeId(1L);

        IssueResponse response =
                issueService.assignIssue(10L, 100L, request);

        assertEquals(1L, response.getAssigneeId());
        assertEquals("Fix login bug", response.getTitle());

        verify(issueRepository).save(issue);
    }

    @Test
    void shouldRejectAssignmentToUserOutsideProject() {
        mockProjectAccess(true);

        Issue issue = createIssue();

        User outsider = new User(
                "outsider", "outsider@example.com", "password-hash"
        );
        ReflectionTestUtils.setField(outsider, "id", 3L);

        when(issueRepository.findById(100L))
                .thenReturn(Optional.of(issue));

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(outsider));

        when(membershipRepository.existsByProjectIdAndUserId(10L, 3L))
                .thenReturn(false);

        AssignIssueRequest request = new AssignIssueRequest();
        request.setAssigneeId(3L);

        assertThrows(
                ForbiddenException.class,
                () -> issueService.assignIssue(10L, 100L, request)
        );

        verify(issueRepository, never()).save(any());
    }

    @Test
    void shouldRejectAssignmentWhenIssueDoesNotExist() {
        mockProjectAccess(true);

        when(issueRepository.findById(999L))
                .thenReturn(Optional.empty());

        AssignIssueRequest request = new AssignIssueRequest();
        request.setAssigneeId(1L);

        assertThrows(
                ResourceNotFoundException.class,
                () -> issueService.assignIssue(10L, 999L, request)
        );

        verify(userRepository, never()).findById(anyLong());
        verify(issueRepository, never()).save(any());
    }

    @Test
    void shouldRejectAssignmentWhenAssigneeDoesNotExist() {
        mockProjectAccess(true);

        when(issueRepository.findById(100L))
                .thenReturn(Optional.of(createIssue()));

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        AssignIssueRequest request = new AssignIssueRequest();
        request.setAssigneeId(999L);

        assertThrows(
                ResourceNotFoundException.class,
                () -> issueService.assignIssue(10L, 100L, request)
        );

        verify(issueRepository, never()).save(any());
    }

    @Test
    void shouldRejectAssignmentWhenIssueBelongsToAnotherProject() {
        mockProjectAccess(true);

        Project anotherProject = new Project(
                "Another Project", "Different project", owner
        );
        ReflectionTestUtils.setField(anotherProject, "id", 20L);

        Issue issue = createIssue();
        issue.setProject(anotherProject);

        when(issueRepository.findById(100L))
                .thenReturn(Optional.of(issue));

        AssignIssueRequest request = new AssignIssueRequest();
        request.setAssigneeId(1L);

        assertThrows(
                ForbiddenException.class,
                () -> issueService.assignIssue(10L, 100L, request)
        );

        verify(userRepository, never()).findById(anyLong());
        verify(issueRepository, never()).save(any());
    }

    @Test
    void shouldRejectAssignmentWhenCurrentUserHasNoProjectAccess() {
        mockProjectAccess(false);

        AssignIssueRequest request = new AssignIssueRequest();
        request.setAssigneeId(1L);

        assertThrows(
                ForbiddenException.class,
                () -> issueService.assignIssue(10L, 100L, request)
        );

        verify(issueRepository, never()).findById(anyLong());
        verify(issueRepository, never()).save(any());
    }

    // --------------------------------------------------
    // ISSUE UPDATE TESTS
    // --------------------------------------------------

    @Test
    void shouldUpdateIssueSuccessfully() {
        mockProjectAccess(true);

        Issue issue = createIssue();

        when(issueRepository.findById(100L))
                .thenReturn(Optional.of(issue));

        when(issueRepository.save(any(Issue.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateIssueRequest request = new UpdateIssueRequest();
        request.setTitle("Updated login issue");
        request.setDescription("Updated description");
        request.setStatus("IN_PROGRESS");
        request.setPriority("HIGH");

        IssueResponse response =
                issueService.updateIssue(10L, 100L, request);

        assertEquals("Updated login issue", response.getTitle());
        assertEquals("Updated description", response.getDescription());
        assertEquals("IN_PROGRESS", response.getStatus());
        assertEquals("HIGH", response.getPriority());

        verify(issueRepository).save(issue);
    }

    @Test
    void shouldRejectIssueUpdateWithBlankTitle() {
        mockProjectAccess(true);

        when(issueRepository.findById(100L))
                .thenReturn(Optional.of(createIssue()));

        UpdateIssueRequest request = new UpdateIssueRequest();
        request.setTitle("   ");

        assertThrows(
                IllegalArgumentException.class,
                () -> issueService.updateIssue(10L, 100L, request)
        );

        verify(issueRepository, never()).save(any());
    }

    @Test
    void shouldRejectIssueUpdateWithInvalidStatus() {
        mockProjectAccess(true);

        when(issueRepository.findById(100L))
                .thenReturn(Optional.of(createIssue()));

        UpdateIssueRequest request = new UpdateIssueRequest();
        request.setStatus("INVALID_STATUS");

        assertThrows(
                IllegalArgumentException.class,
                () -> issueService.updateIssue(10L, 100L, request)
        );

        verify(issueRepository, never()).save(any());
    }

    @Test
    void shouldRejectIssueUpdateWithInvalidPriority() {
        mockProjectAccess(true);

        when(issueRepository.findById(100L))
                .thenReturn(Optional.of(createIssue()));

        UpdateIssueRequest request = new UpdateIssueRequest();
        request.setPriority("INVALID_PRIORITY");

        assertThrows(
                IllegalArgumentException.class,
                () -> issueService.updateIssue(10L, 100L, request)
        );

        verify(issueRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWhenIssueDoesNotExist() {
        mockProjectAccess(true);

        when(issueRepository.findById(999L))
                .thenReturn(Optional.empty());

        UpdateIssueRequest request = new UpdateIssueRequest();
        request.setTitle("Updated title");

        assertThrows(
                ResourceNotFoundException.class,
                () -> issueService.updateIssue(10L, 999L, request)
        );

        verify(issueRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWhenIssueBelongsToAnotherProject() {
        mockProjectAccess(true);

        Project anotherProject = new Project(
                "Another Project", "Different project", owner
        );
        ReflectionTestUtils.setField(anotherProject, "id", 20L);

        Issue issue = createIssue();
        issue.setProject(anotherProject);

        when(issueRepository.findById(100L))
                .thenReturn(Optional.of(issue));

        UpdateIssueRequest request = new UpdateIssueRequest();
        request.setTitle("Updated title");

        assertThrows(
                ForbiddenException.class,
                () -> issueService.updateIssue(10L, 100L, request)
        );

        verify(issueRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWhenCurrentUserHasNoProjectAccess() {
        mockProjectAccess(false);

        UpdateIssueRequest request = new UpdateIssueRequest();
        request.setTitle("Updated title");

        assertThrows(
                ForbiddenException.class,
                () -> issueService.updateIssue(10L, 100L, request)
        );

        verify(issueRepository, never()).findById(anyLong());
        verify(issueRepository, never()).save(any());
    }
}
