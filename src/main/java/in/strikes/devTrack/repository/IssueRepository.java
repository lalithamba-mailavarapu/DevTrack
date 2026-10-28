
package in.strikes.devTrack.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import in.strikes.devTrack.entity.Issue;


public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByProjectId(Long projectId);

    Page<Issue> findByProjectId(Long projectId, Pageable pageable);

    List<Issue> findByAssigneeId(Long assigneeId);

    List<Issue> findByReporterId(Long reporterId);

    List<Issue> findByProjectIdAndStatus(
            Long projectId, Issue.Status status);
    Page<Issue> findByProjectIdAndStatus(
        Long projectId, Issue.Status status, Pageable pageable);

    Page<Issue> findByProjectIdAndPriority(
            Long projectId, Issue.Priority priority, Pageable pageable);

    Page<Issue> findByProjectIdAndStatusAndPriority(
            Long projectId,
            Issue.Status status,
            Issue.Priority priority,
            Pageable pageable);

}
