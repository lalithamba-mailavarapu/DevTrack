
package in.strikes.devTrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.strikes.devTrack.entity.ProjectTechnology;

public interface ProjectTechnologyRepository
        extends JpaRepository<ProjectTechnology, Long> {

    List<ProjectTechnology> findByProjectId(Long projectId);
}
