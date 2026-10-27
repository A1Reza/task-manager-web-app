package ir.reza.taskmanager.repository.project;

import ir.reza.taskmanager.entity.Project;
import ir.reza.taskmanager.repository.base.BaseRepository;

public class ProjectRepositoryImpl extends BaseRepository<Project>
        implements ProjectRepository {

    public ProjectRepositoryImpl() {
        super(Project.class);
    }

    @Override
    protected void copyFields(Project source, Project target) {
        target.setName(source.getName());
        target.setDescription(source.getDescription());
    }
}