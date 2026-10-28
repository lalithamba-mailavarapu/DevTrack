
package in.strikes.devTrack.repository;

import in.strikes.devTrack.entity.ProjectMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMembershipRepository
        extends JpaRepository<ProjectMembership, Long> {

    List<ProjectMembership> findByUserId(Long userId);

    List<ProjectMembership> findByProjectId(Long projectId);

    Optional<ProjectMembership> findByProjectIdAndUserId(
            Long projectId, Long userId);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
}
