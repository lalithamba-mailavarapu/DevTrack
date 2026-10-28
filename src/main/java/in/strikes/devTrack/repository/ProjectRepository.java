
package in.strikes.devTrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.strikes.devTrack.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerId(Long ownerId);
}
