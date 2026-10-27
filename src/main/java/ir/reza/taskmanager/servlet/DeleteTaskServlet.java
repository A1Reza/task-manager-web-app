package ir.reza.taskmanager.servlet;

import ir.reza.taskmanager.exception.ResourceNotFoundException;
import ir.reza.taskmanager.service.TaskService;
import ir.reza.taskmanager.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/tasks/delete")
public class DeleteTaskServlet extends HttpServlet {

    private TaskService taskService;

    @Override
    public void init() throws ServletException {
        taskService = (TaskService) getServletContext()
                .getAttribute("taskService");

        if (taskService == null) {
            throw new ServletException("TaskService is not initialized.");
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        try {
            String idParameter = request.getParameter("id");

            if (idParameter == null || idParameter.isBlank()) {
                JsonUtil.error(response, 400, "Task ID is required.");
                return;
            }

            long id = Long.parseLong(idParameter);

            if (id <= 0) {
                JsonUtil.error(response, 400, "Task ID must be positive.");
                return;
            }

            taskService.delete(id);

            JsonUtil.send(
                    response,
                    200,
                    Map.of("message", "Task deleted successfully", "id", id)
            );

        } catch (NumberFormatException exception) {
            JsonUtil.error(response, 400, "Invalid task ID.");
        } catch (ResourceNotFoundException exception) {
            JsonUtil.error(response, 404, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            JsonUtil.error(response, 400, exception.getMessage());
        }
    }
}
