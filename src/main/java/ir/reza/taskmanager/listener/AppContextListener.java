package ir.reza.taskmanager.listener;

import ir.reza.taskmanager.dto.request.ProjectRequestDto;
import ir.reza.taskmanager.dto.request.TaskRequestDto;
import ir.reza.taskmanager.dto.request.UserRequestDto;
import ir.reza.taskmanager.dto.response.ProjectResponseDto;
import ir.reza.taskmanager.dto.response.UserResponseDto;
import ir.reza.taskmanager.repository.project.ProjectRepository;
import ir.reza.taskmanager.repository.project.ProjectRepositoryImpl;
import ir.reza.taskmanager.repository.task.TaskRepository;
import ir.reza.taskmanager.repository.task.TaskRepositoryImpl;
import ir.reza.taskmanager.repository.user.UserRepository;
import ir.reza.taskmanager.repository.user.UserRepositoryImpl;
import ir.reza.taskmanager.service.LogService;
import ir.reza.taskmanager.service.ProjectService;
import ir.reza.taskmanager.service.SessionService;
import ir.reza.taskmanager.service.TaskService;
import ir.reza.taskmanager.service.UserService;
import ir.reza.taskmanager.util.HibernateUtil;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
/*ServletContext is where shared Repositories and Services are stored throughout the application's lifetime.*/

@WebListener
public class AppContextListener implements ServletContextListener {

    private static final String TASK_REPOSITORY = "taskRepository";
    private static final String USER_REPOSITORY = "userRepository";
    private static final String PROJECT_REPOSITORY = "projectRepository";

    private static final String TASK_SERVICE = "taskService";
    private static final String USER_SERVICE = "userService";
    private static final String PROJECT_SERVICE = "projectService";
    private static final String LOG_SERVICE = "logService";
    private static final String SESSION_SERVICE = "sessionService";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();

        LogService logService = new LogService();
        logService.setPrefix("TASK-MANAGER");

        try {
            logService.log("Application starting...");

            TaskRepository taskRepository = new TaskRepositoryImpl();
            UserRepository userRepository = new UserRepositoryImpl();
            ProjectRepository projectRepository = new ProjectRepositoryImpl();


            TaskService taskService = new TaskService(
                    taskRepository,
                    userRepository,
                    projectRepository
            );

            UserService userService = new UserService(userRepository);
            ProjectService projectService =
                    new ProjectService(projectRepository);

            SessionService sessionService = new SessionService();

            context.setAttribute(TASK_REPOSITORY, taskRepository);
            context.setAttribute(USER_REPOSITORY, userRepository);
            context.setAttribute(PROJECT_REPOSITORY, projectRepository);

            context.setAttribute(TASK_SERVICE, taskService);
            context.setAttribute(USER_SERVICE, userService);
            context.setAttribute(PROJECT_SERVICE, projectService);
            context.setAttribute(LOG_SERVICE, logService);
            context.setAttribute(SESSION_SERVICE, sessionService);

            initializeData(userService, projectService, taskService, logService);

            logService.log("Application started successfully.");

        } catch (RuntimeException exception) {
            logService.log(
                    "Application startup failed: " + exception.getMessage()
            );

            HibernateUtil.shutdown();
            throw exception;
        }
    }

    private void initializeData(
            UserService userService,
            ProjectService projectService,
            TaskService taskService,
            LogService logService
    ) {
        boolean databaseIsEmpty =
                userService.findAll().isEmpty()
                        && projectService.findAll().isEmpty()
                        && taskService.findAll().isEmpty();

        if (!databaseIsEmpty) {
            logService.log("Initial data skipped; database is not empty.");
            return;
        }

        UserResponseDto user1 = userService.add(
                new UserRequestDto("Reza", "reza@example.com")
        );

        UserResponseDto user2 = userService.add(
                new UserRequestDto("Mehrnaz", "Mehrnaz@example.com")
        );

        ProjectResponseDto project1 = projectService.add(
                new ProjectRequestDto(
                        "Task Manager",
                        "Application for managing tasks and projects."
                )
        );

        ProjectResponseDto project2 = projectService.add(
                new ProjectRequestDto(
                        "Website Development",
                        "Tasks related to website development."
                )
        );

        taskService.add(
                new TaskRequestDto(
                        "Design database",
                        120,
                        user1.id(),
                        project1.id()
                )
        );

        taskService.add(
                new TaskRequestDto(
                        "Implement services",
                        180,
                        user2.id(),
                        project1.id()
                )
        );

        taskService.add(
                new TaskRequestDto(
                        "Create homepage",
                        90,
                        user1.id(),
                        project2.id()
                )
        );

        logService.log("Initial data created successfully.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        ServletContext context = event.getServletContext();

        Object service = context.getAttribute(LOG_SERVICE);

        if (service instanceof LogService logService) {
            logService.log("Application shutting down. Goodbye!");
        }

        HibernateUtil.shutdown();
    }
}