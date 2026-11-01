
package in.strikes.devTrack.dto;

import in.strikes.devTrack.entity.ProjectTechnology.TechnologyType;

public class ProjectTechnologyResponse {

    private Long id;
    private String technologyName;
    private TechnologyType technologyType;

    public ProjectTechnologyResponse(
            Long id,
            String technologyName,
            TechnologyType technologyType) {

        this.id = id;
        this.technologyName = technologyName;
        this.technologyType = technologyType;
    }

    public Long getId() {
        return id;
    }

    public String getTechnologyName() {
        return technologyName;
    }

    public TechnologyType getTechnologyType() {
        return technologyType;
    }
}
