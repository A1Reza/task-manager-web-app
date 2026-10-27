package ir.reza.taskmanager.command;

import ir.reza.taskmanager.dto.response.ProjectResponseDto;
import ir.reza.taskmanager.entity.Project;

public class ProjectToProjectResponseDto {

    public ProjectResponseDto execute(Project project) {
        return new ProjectResponseDto(
                project.getId(),
                project.getName(),
                project.getDescription()
        );
    }
}