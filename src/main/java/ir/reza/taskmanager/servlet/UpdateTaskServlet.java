package ir.reza.taskmanager.servlet;

import ir.reza.taskmanager.dto.request.TaskRequestDto;
import ir.reza.taskmanager.dto.response.ProjectResponseDto;
import ir.reza.taskmanager.dto.response.TaskResponseDto;
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

@WebServlet("/tasks/update")
public class UpdateTaskServlet extends HttpServlet {

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

        if (taskService == null
                || userService == null
                || projectService == null
                || logService == null) {
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

        Long id;

        try {
            id = parseRequiredId(request.getParameter("id"));
        } catch (IllegalArgumentException exception) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid task ID."
            );
            return;
        }

        try {
            TaskResponseDto task = taskService.findById(id);

            renderForm(
                    request,
                    response,
                    task,
                    userService.findAll(),
                    projectService.findAll(),
                    null
            );

        } catch (ResourceNotFoundException exception) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    exception.getMessage()
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        Long id;

        try {
            id = parseRequiredId(request.getParameter("id"));
        } catch (IllegalArgumentException exception) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid task ID."
            );
            return;
        }

        String title = request.getParameter("title");
        String minutesParam = request.getParameter("estimatedMinutes");
        String assigneeParam = request.getParameter("assigneeId");
        String projectParam = request.getParameter("projectId");

        try {
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException(
                        "Task title is required."
                );
            }

            int minutes = Integer.parseInt(minutesParam);

            if (minutes <= 0) {
                throw new IllegalArgumentException(
                        "Estimated minutes must be greater than zero."
                );
            }

            Long assigneeId = parseOptionalId(assigneeParam);
            Long projectId = parseOptionalId(projectParam);

            TaskRequestDto dto = new TaskRequestDto(
                    title.trim(),
                    minutes,
                    assigneeId,
                    projectId
            );

            taskService.update(id, dto);

            logService.log("Task updated successfully. ID: " + id);

            response.sendRedirect(
                    request.getContextPath() + "/tasks"
            );

        } catch (NumberFormatException exception) {
            renderErrorForm(
                    request,
                    response,
                    id,
                    "Please enter valid numeric values."
            );

        } catch (IllegalArgumentException exception) {
            renderErrorForm(
                    request,
                    response,
                    id,
                    exception.getMessage()
            );

        } catch (InvalidReferenceException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            renderErrorForm(
                    request,
                    response,
                    id,
                    exception.getMessage()
            );

        } catch (ResourceNotFoundException exception) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    exception.getMessage()
            );
        }
    }

    private Long parseRequiredId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Task ID is required."
            );
        }

        long id = Long.parseLong(value);

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Task ID must be greater than zero."
            );
        }

        return id;
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

    private void renderErrorForm(
            HttpServletRequest request,
            HttpServletResponse response,
            Long id,
            String message
    ) throws IOException {

        try {
            TaskResponseDto task = taskService.findById(id);

            renderForm(
                    request,
                    response,
                    task,
                    userService.findAll(),
                    projectService.findAll(),
                    message
            );

        } catch (ResourceNotFoundException exception) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    exception.getMessage()
            );
        }
    }

    private void renderForm(
            HttpServletRequest request,
            HttpServletResponse response,
            TaskResponseDto task,
            List<UserResponseDto> users,
            List<ProjectResponseDto> projects,
            String errorMessage
    ) throws IOException {

        String contextPath = request.getContextPath();

        try (PrintWriter out = response.getWriter()) {
            out.println("""
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport"
                              content="width=device-width, initial-scale=1.0">
                        <title>Update Task</title>
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
                            .error {
                                color: #b42318;
                            }
                        </style>
                    </head>
                    <body>
                        <h1>Update Task</h1>
                    """);

            if (errorMessage != null) {
                out.println("<p class=\"error\">"
                        + escapeHtml(errorMessage)
                        + "</p>");
            }

            out.println("<form method=\"post\" action=\""
                    + contextPath
                    + "/tasks/update\">");

            out.println("<input type=\"hidden\" name=\"id\" value=\""
                    + task.id() + "\">");

            out.println("""
                    <label for="title">Task Title</label>
                    """);

            out.println("<input type=\"text\" id=\"title\" name=\"title\""
                    + " maxlength=\"255\" required value=\""
                    + escapeHtml(task.title()) + "\">");

            out.println("""
                    <label for="estimatedMinutes">Estimated Minutes</label>
                    """);

            out.println("<input type=\"number\" id=\"estimatedMinutes\""
                    + " name=\"estimatedMinutes\" min=\"1\" required value=\""
                    + task.estimatedMinutes() + "\">");

            out.println("""
                    <label for="assigneeId">Assignee</label>
                    <select id="assigneeId" name="assigneeId">
                    """);

            out.println("<option value=\"\""
                    + (task.assigneeId() == null ? " selected" : "")
                    + ">Unassigned</option>");

            for (UserResponseDto user : users) {
                String selected = user.id().equals(task.assigneeId())
                        ? " selected"
                        : "";

                out.println("<option value=\"" + user.id() + "\""
                        + selected + ">"
                        + escapeHtml(user.name())
                        + "</option>");
            }

            out.println("""
                    </select>
                    <label for="projectId">Project</label>
                    <select id="projectId" name="projectId">
                    """);

            out.println("<option value=\"\""
                    + (task.projectId() == null ? " selected" : "")
                    + ">No Project</option>");

            for (ProjectResponseDto project : projects) {
                String selected = project.id().equals(task.projectId())
                        ? " selected"
                        : "";

                out.println("<option value=\"" + project.id() + "\""
                        + selected + ">"
                        + escapeHtml(project.name())
                        + "</option>");
            }

            out.println("""
                    </select>
                    <button type="submit">Save Changes</button>
                    """);

            out.println("<a class=\"button\" href=\""
                    + contextPath
                    + "/tasks\">Cancel</a>");

            out.println("""
                    </form>
                    </body>
                    </html>
                    """);
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
