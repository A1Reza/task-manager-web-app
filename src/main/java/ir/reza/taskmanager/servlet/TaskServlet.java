package ir.reza.taskmanager.servlet;

import ir.reza.taskmanager.dto.response.TaskResponseDto;
import ir.reza.taskmanager.dto.response.UserResponseDto;
import ir.reza.taskmanager.exception.ResourceNotFoundException;
import ir.reza.taskmanager.service.LogService;
import ir.reza.taskmanager.service.SessionService;
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

@WebServlet("/tasks")
public class TaskServlet extends HttpServlet {

    private TaskService taskService;
    private UserService userService;
    private SessionService sessionService;
    private LogService logService;

    @Override
    public void init() throws ServletException {
        taskService = (TaskService) getServletContext()
                .getAttribute("taskService");

        userService = (UserService) getServletContext()
                .getAttribute("userService");

        sessionService = (SessionService) getServletContext()
                .getAttribute("sessionService");

        logService = (LogService) getServletContext()
                .getAttribute("logService");

        if (taskService == null
                || userService == null
                || sessionService == null
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

        String assigneeIdParam = request.getParameter("assigneeId");
        String errorMessage = null;

        List<TaskResponseDto> tasks = List.of();
        List<UserResponseDto> users = userService.findAll();

        try {
            if (assigneeIdParam == null || assigneeIdParam.isBlank()) {
                tasks = taskService.findAll();
            } else {
                Long assigneeId = Long.parseLong(assigneeIdParam);
                tasks = taskService.findByAssignee(assigneeId);
            }
        } catch (NumberFormatException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            errorMessage = "Invalid assignee ID.";
        } catch (ResourceNotFoundException exception) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            errorMessage = exception.getMessage();
        }

        int visitCount = sessionService.incrementVisitCount(
                request.getSession()
        );

        logService.log("Task list page requested.");

        String contextPath = request.getContextPath();

        try (PrintWriter out = response.getWriter()) {
            out.println("""
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport"
                              content="width=device-width, initial-scale=1.0">
                        <title>Task Manager</title>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                margin: 30px;
                                background: #f4f6f9;
                            }
                            h1 { color: #243447; }
                            table {
                                width: 100%;
                                border-collapse: collapse;
                                background: white;
                                margin-top: 20px;
                            }
                            th, td {
                                padding: 12px;
                                border: 1px solid #ddd;
                                text-align: left;
                            }
                            th { background: #243447; color: white; }
                            .message {
                                color: #b42318;
                                margin: 15px 0;
                            }
                            .actions { white-space: nowrap; }
                            .button {
                                display: inline-block;
                                padding: 8px 12px;
                                margin: 4px 2px;
                                background: #2463eb;
                                color: white;
                                text-decoration: none;
                                border: none;
                                cursor: pointer;
                            }
                            .delete-button { background: #c62828; }
                            form { margin-top: 15px; }
                        </style>
                    </head>
                    <body>
                        <h1>Task Manager</h1>
                    """);

            out.println("<p>Session visits: " + visitCount + "</p>");

            out.println("<p><a class='button' href='"
                    + contextPath + "/tasks/add'>Add New Task</a></p>");

            out.println("""
                    <form method="get" action="%s/tasks">
                        <label for="assigneeId">Filter by assignee:</label>
                        <select name="assigneeId" id="assigneeId">
                            <option value="">All tasks</option>
                    """.formatted(contextPath));

            for (UserResponseDto user : users) {
                String selected = user.id().toString()
                        .equals(assigneeIdParam) ? " selected" : "";

                out.println("<option value=\"" + user.id() + "\""
                        + selected + ">"
                        + escapeHtml(user.name())
                        + "</option>");
            }

            out.println("""
                        </select>
                        <button type="submit">Filter</button>
                        <a href="%s/tasks">Clear</a>
                    </form>
                    """.formatted(contextPath));

            if (errorMessage != null) {
                out.println("<p class='message'>"
                        + escapeHtml(errorMessage) + "</p>");
            } else {
                out.println("""
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Title</th>
                                    <th>Estimated Minutes</th>
                                    <th>Assignee</th>
                                    <th>Project</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                        """);

                if (tasks.isEmpty()) {
                    out.println("""
                            <tr>
                                <td colspan="6">No tasks found.</td>
                            </tr>
                            """);
                }

                for (TaskResponseDto task : tasks) {
                    String assigneeName = task.assigneeName() == null
                            ? "Unassigned"
                            : task.assigneeName();

                    String projectName = task.projectName() == null
                            ? "No Project"
                            : task.projectName();

                    out.println("<tr>");
                    out.println("<td>" + task.id() + "</td>");
                    out.println("<td>" + escapeHtml(task.title()) + "</td>");
                    out.println("<td>" + task.estimatedMinutes() + "</td>");
                    out.println("<td>" + escapeHtml(assigneeName) + "</td>");
                    out.println("<td>" + escapeHtml(projectName) + "</td>");

                    out.println("<td class='actions'>");

                    out.println("<a class='button' href='"
                            + contextPath + "/tasks/update?id="
                            + task.id() + "'>Edit</a>");

                    out.println("<form method='post' action='"
                            + contextPath + "/tasks/delete'>");

                    out.println("<input type='hidden' name='id' value='"
                            + task.id() + "'>");

                    out.println("""
                            <button class="button delete-button"
                                    type="submit"
                                    onclick="return confirm('Delete this task?');">
                                Delete
                            </button>
                            """);

                    out.println("</form>");
                    out.println("</td>");
                    out.println("</tr>");
                }

                out.println("</tbody></table>");
            }

            out.println("""
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