package ir.reza.taskmanager.command;

import ir.reza.taskmanager.dto.request.TaskRequestDto;
import ir.reza.taskmanager.entity.Project;
import ir.reza.taskmanager.entity.Task;
import ir.reza.taskmanager.entity.User;

public class TaskRequestDtoToTask {

    public Task execute(
            TaskRequestDto dto,
            User assignee,
            Project project
    ) {
        return Task.builder()
                .title(dto.title())
                .estimatedMinutes(dto.estimatedMinutes())
                .assignee(assignee)
                .project(project)
                .build();
    }
}