
package ir.reza.taskmanager.servlet;

import ir.reza.taskmanager.exception.ResourceNotFoundException;
import ir.reza.taskmanager.service.LogService;
import ir.reza.taskmanager.service.TaskService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/tasks/delete")
public class DeleteTaskServlet extends HttpServlet {

    private TaskService taskService;
    private LogService logService;

    @Override
    public void init() throws ServletException {
        taskService = (TaskService) getServletContext()
                .getAttribute("taskService");

        logService = (LogService) getServletContext()
                .getAttribute("logService");

        if (taskService == null || logService == null) {
            throw new ServletException(
                    "Required services were not initialized."
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String idParam = request.getParameter("id");

        if (idParam == null || idParam.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Task ID is required."
            );
            return;
        }

        final long id;

        try {
            id = Long.parseLong(idParam);

            if (id <= 0) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Task ID must be greater than zero."
                );
                return;
            }
        } catch (NumberFormatException exception) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid task ID."
            );
            return;
        }

        try {
            taskService.delete(id);

            logService.log("Task deleted successfully. ID: " + id);

            response.sendRedirect(
                    request.getContextPath() + "/tasks"
            );

        } catch (ResourceNotFoundException exception) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/html;charset=UTF-8");

            try (PrintWriter out = response.getWriter()) {
                out.println("""
                        <!DOCTYPE html>
                        <html lang="en">
                        <head>
                            <meta charset="UTF-8">
                            <title>Task Not Found</title>
                        </head>
                        <body>
                            <h1>Task Not Found</h1>
                        """);

                out.println("<p>"
                        + escapeHtml(exception.getMessage())
                        + "</p>");

                out.println("<a href=\""
                        + request.getContextPath()
                        + "/tasks\">Back to Tasks</a>");

                out.println("""
                        </body>
                        </html>
                        """);
            }
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        response.sendError(
                HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Use POST to delete a task."
        );
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
