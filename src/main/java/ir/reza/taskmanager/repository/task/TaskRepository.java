package ir.reza.taskmanager.repository.task;

import ir.reza.taskmanager.entity.Task;
import ir.reza.taskmanager.repository.base.Repository;

import java.util.List;

public interface TaskRepository extends Repository<Task> {

    List<Task> findByAssignee(Long assigneeId);
}