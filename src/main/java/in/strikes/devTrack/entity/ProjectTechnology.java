
package in.strikes.devTrack.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_technologies")
public class ProjectTechnology {

    public enum TechnologyType {
        LANGUAGE,
        FRAMEWORK,
        DATABASE,
        TOOL,
        OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String technologyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TechnologyType technologyType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public ProjectTechnology() {
    }

    public ProjectTechnology(
            String technologyName,
            TechnologyType technologyType,
            Project project) {
        this.technologyName = technologyName;
        this.technologyType = technologyType;
        this.project = project;
    }

    public Long getId() {
        return id;
    }

    public String getTechnologyName() {
        return technologyName;
    }

    public void setTechnologyName(String technologyName) {
        this.technologyName = technologyName;
    }

    public TechnologyType getTechnologyType() {
        return technologyType;
    }

    public void setTechnologyType(TechnologyType technologyType) {
        this.technologyType = technologyType;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
