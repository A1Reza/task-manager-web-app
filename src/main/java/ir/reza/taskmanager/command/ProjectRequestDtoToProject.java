package ir.reza.taskmanager.command;

import ir.reza.taskmanager.dto.request.ProjectRequestDto;
import ir.reza.taskmanager.entity.Project;

public class ProjectRequestDtoToProject {

    public Project execute(ProjectRequestDto dto) {
        return Project.builder()
                .name(dto.name())
                .description(dto.description())
                .build();
    }
}