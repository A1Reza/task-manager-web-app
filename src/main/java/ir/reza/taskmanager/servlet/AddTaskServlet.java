package ir.reza.taskmanager.servlet;

import ir.reza.taskmanager.dto.request.TaskRequestDto;
import ir.reza.taskmanager.dto.response.ProjectResponseDto;
import ir.reza.taskmanager.dto.response.UserResponseDto;
import ir.reza.taskmanager.exception.InvalidReferenceException;
import ir.reza.taskmanager.exception.ResourceNotFoundException;
import ir.reza.taskmanager.service.LogService;
import ir.reza.taskmanager.service.ProjectService;
import ir.reza.taskmanager.service.TaskService;
import ir.reza.taskmanager.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/tasks/add")
public class AddTaskServlet extends HttpServlet {

    private TaskService taskService;
    private UserService userService;
    private ProjectService projectService;
    private LogService logService;

    @Override
    public void init() throws ServletException {
        taskService = (TaskService) getServletContext()
                .getAttribute("taskService");

        userService = (UserService) getServletContext()
                .getAttribute("userService");

        projectService = (ProjectService) getServletContext()
                .getAttribute("projectService");

        logService = (LogService) getServletContext()
                .getAttribute("logService");

        if (taskService == null || userService == null
                || projectService == null || logService == null) {
            throw new ServletException(
                    "Required services were not initialized."
            );
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        List<UserResponseDto> users = userService.findAll();
        List<ProjectResponseDto> projects = projectService.findAll();

        renderForm(response, users, projects, null);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String title = request.getParameter("title");
        String estimatedMinutesParam =
                request.getParameter("estimatedMinutes");
        String assigneeIdParam = request.getParameter("assigneeId");
        String projectIdParam = request.getParameter("projectId");

        try {
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException(
                        "Task title is required."
                );
            }

            int estimatedMinutes =
                    Integer.parseInt(estimatedMinutesParam);

            if (estimatedMinutes <= 0) {
                throw new IllegalArgumentException(
                        "Estimated minutes must be greater than zero."
                );
            }

            Long assigneeId = parseOptionalId(assigneeIdParam);
            Long projectId = parseOptionalId(projectIdParam);

            TaskRequestDto dto = new TaskRequestDto(
                    title.trim(),
                    estimatedMinutes,
                    assigneeId,
                    projectId
            );

            taskService.add(dto);

            logService.log("Task created successfully.");

            /*Post/Redirect/Get prevents accidental resubmission on refresh.*/
            response.sendRedirect(
                    request.getContextPath() + "/tasks"
            );

        } catch (NumberFormatException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            renderForm(
                    response,
                    userService.findAll(),
                    projectService.findAll(),
                    "Please enter valid numeric values."
            );

        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            renderForm(
                    response,
                    userService.findAll(),
                    projectService.findAll(),
                    exception.getMessage()
            );

        } catch (InvalidReferenceException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            renderForm(
                    response,
                    userService.findAll(),
                    projectService.findAll(),
                    exception.getMessage()
            );

        } catch (ResourceNotFoundException exception) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);

            renderForm(
                    response,
                    userService.findAll(),
                    projectService.findAll(),
                    exception.getMessage()
            );
        }
    }

    private Long parseOptionalId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        long id = Long.parseLong(value);

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "IDs must be greater than zero."
            );
        }

        return id;
    }

    private void renderForm(
            HttpServletResponse response,
            List<UserResponseDto> users,
            List<ProjectResponseDto> projects,
            String errorMessage
    ) throws IOException {

        try (PrintWriter out = response.getWriter()) {
            out.println("""
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport"
                              content="width=device-width, initial-scale=1.0">
                        <title>Add Task</title>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                max-width: 650px;
                                margin: 40px auto;
                                padding: 0 20px;
                                background: #f4f6f9;
                            }
                            form {
                                background: white;
                                padding: 24px;
                                border-radius: 8px;
                            }
                            label {
                                display: block;
                                margin-top: 16px;
                                font-weight: bold;
                            }
                            input, select {
                                box-sizing: border-box;
                                width: 100%;
                                padding: 10px;
                                margin-top: 6px;
                            }
                            button, .button {
                                display: inline-block;
                                margin-top: 20px;
                                padding: 10px 14px;
                                background: #2463eb;
                                color: white;
                                border: none;
                                text-decoration: none;
                                cursor: pointer;
                            }
                            .error { color: #b42318; }
                        </style>
                    </head>
                    <body>
                        <h1>Add New Task</h1>
                    """);

            if (errorMessage != null) {
                out.println("<p class=\"error\">"
                        + escapeHtml(errorMessage) + "</p>");
            }

            out.println("""
                    <form method="post">
                        <label for="title">Task Title</label>
                        <input type="text" id="title" name="title"
                               required maxlength="255">

                        <label for="estimatedMinutes">
                            Estimated Minutes
                        </label>
                        <input type="number" id="estimatedMinutes"
                               name="estimatedMinutes"
                               min="1" required>

                        <label for="assigneeId">Assignee</label>
                        <select id="assigneeId" name="assigneeId">
                            <option value="">Unassigned</option>
                    """);

            for (UserResponseDto user : users) {
                out.println("<option value=\"" + user.id() + "\">"
                        + escapeHtml(user.name())
                        + "</option>");
            }

            out.println("""
                        </select>

                        <label for="projectId">Project</label>
                        <select id="projectId" name="projectId">
                            <option value="">No Project</option>
                    """);

            for (ProjectResponseDto project : projects) {
                out.println("<option value=\"" + project.id() + "\">"
                        + escapeHtml(project.name())
                        + "</option>");
            }

            out.println("""
                        </select>

                        <button type="submit">Create Task</button>
                        <a class="button" href="%s/tasks">Cancel</a>
                    </form>
                    </body>
                    </html>
                    """.formatted(getServletContext().getContextPath()));
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}