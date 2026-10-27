package ir.reza.taskmanager.service;

import ir.reza.taskmanager.command.TaskRequestDtoToTask;
import ir.reza.taskmanager.command.TaskToTaskResponseDto;
import ir.reza.taskmanager.dto.request.TaskRequestDto;
import ir.reza.taskmanager.dto.response.TaskResponseDto;
import ir.reza.taskmanager.entity.Project;
import ir.reza.taskmanager.entity.Task;
import ir.reza.taskmanager.entity.User;
import ir.reza.taskmanager.exception.InvalidReferenceException;
import ir.reza.taskmanager.exception.ResourceNotFoundException;
import ir.reza.taskmanager.repository.project.ProjectRepository;
import ir.reza.taskmanager.repository.task.TaskRepository;
import ir.reza.taskmanager.repository.user.UserRepository;

import java.util.List;

public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    private final TaskRequestDtoToTask requestConverter =
            new TaskRequestDtoToTask();

    private final TaskToTaskResponseDto responseConverter =
            new TaskToTaskResponseDto();

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    public List<TaskResponseDto> findAll() {
        return taskRepository.findAll()
                .stream()
                .map(responseConverter::execute)
                .toList();
    }

    public TaskResponseDto findById(Long id) {
        Task task = taskRepository.findById(id);

        if (task == null) {
            throw new ResourceNotFoundException(
                    "Task not found with id: " + id
            );
        }

        return responseConverter.execute(task);
    }

    public List<TaskResponseDto> findByAssignee(Long assigneeId) {
        if (userRepository.findById(assigneeId) == null) {
            throw new ResourceNotFoundException(
                    "User not found with id: " + assigneeId
            );
        }

        return taskRepository.findByAssignee(assigneeId)
                .stream()
                .map(responseConverter::execute)
                .toList();
    }

    public TaskResponseDto add(TaskRequestDto dto) {
        User assignee = findAssignee(dto.assigneeId());
        Project project = findProject(dto.projectId());

        Task task = requestConverter.execute(dto, assignee, project);
        Task savedTask = taskRepository.add(task);

        return responseConverter.execute(savedTask);
    }

    public TaskResponseDto update(Long id, TaskRequestDto dto) {
        if (taskRepository.findById(id) == null) {
            throw new ResourceNotFoundException(
                    "Task not found with id: " + id
            );
        }

        User assignee = findAssignee(dto.assigneeId());
        Project project = findProject(dto.projectId());

        Task updatedTask = requestConverter.execute(dto, assignee, project);
        Task result = taskRepository.update(id, updatedTask);

        if (result == null) {
            throw new ResourceNotFoundException(
                    "Task not found with id: " + id
            );
        }

        return responseConverter.execute(taskRepository.findById(id));
    }

    public void delete(Long id) {
        boolean deleted = taskRepository.delete(id);

        if (!deleted) {
            throw new ResourceNotFoundException(
                    "Task not found with id: " + id
            );
        }
    }

    private User findAssignee(Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }

        User user = userRepository.findById(assigneeId);

        if (user == null) {
            throw new InvalidReferenceException(
                    "Invalid assigneeId: " + assigneeId
            );
        }

        return user;
    }

    private Project findProject(Long projectId) {
        if (projectId == null) {
            return null;
        }

        Project project = projectRepository.findById(projectId);

        if (project == null) {
            throw new InvalidReferenceException(
                    "Invalid projectId: " + projectId
            );
        }

        return project;
    }
}