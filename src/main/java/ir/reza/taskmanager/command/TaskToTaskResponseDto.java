package ir.reza.taskmanager.command;

import ir.reza.taskmanager.dto.response.TaskResponseDto;
import ir.reza.taskmanager.entity.Task;

public class TaskToTaskResponseDto {

    public TaskResponseDto execute(Task task) {

        return new TaskResponseDto(
                task.getId(),
                task.getTitle(),
                task.getEstimatedMinutes(),
                task.getAssignee() == null
                        ? null : task.getAssignee().getId(),
                task.getAssignee() == null
                        ? null : task.getAssignee().getName(),
                task.getProject() == null
                        ? null : task.getProject().getId(),
                task.getProject() == null
                        ? null : task.getProject().getName()
        );
    }
}