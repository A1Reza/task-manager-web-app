package ir.reza.taskmanager.command;

import ir.reza.taskmanager.dto.request.TaskRequestDto;
import ir.reza.taskmanager.entity.Project;
import ir.reza.taskmanager.entity.Task;
import ir.reza.taskmanager.entity.User;
import jakarta.persistence.EntityManager;

public class TaskRequestDtoToTask {

    public Task execute(
            TaskRequestDto dto,
            EntityManager em) {

        User assignee = dto.assigneeId() == null
                ? null
                : em.find(User.class, dto.assigneeId());

        Project project = dto.projectId() == null
                ? null
                : em.find(Project.class, dto.projectId());

        return Task.builder()
                .title(dto.title())
                .estimatedMinutes(dto.estimatedMinutes())
                .assignee(assignee)
                .project(project)
                .build();
    }
}