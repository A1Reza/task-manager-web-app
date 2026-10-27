package ir.reza.taskmanager.servlet;

import ir.reza.taskmanager.dto.response.TaskResponseDto;
import ir.reza.taskmanager.exception.ResourceNotFoundException;
import ir.reza.taskmanager.service.TaskService;
import ir.reza.taskmanager.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/tasks")
public class TaskServlet extends HttpServlet {

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
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        try {
            String id = request.getParameter("id");
            String assigneeId = request.getParameter("assigneeId");

            if (id != null) {
                long taskId = Long.parseLong(id);

                if (taskId <= 0) {
                    JsonUtil.error(response, 400, "Task ID must be positive.");
                    return;
                }

                TaskResponseDto task = taskService.findById(taskId);
                JsonUtil.send(response, 200, task);
                return;
            }

            if (assigneeId != null) {
                long userId = Long.parseLong(assigneeId);

                if (userId <= 0) {
                    JsonUtil.error(response, 400, "Assignee ID must be positive.");
                    return;
                }

                List<TaskResponseDto> tasks =
                        taskService.findByAssignee(userId);

                JsonUtil.send(response, 200, tasks);
                return;
            }

            JsonUtil.send(response, 200, taskService.findAll());

        } catch (NumberFormatException exception) {
            JsonUtil.error(response, 400, "Invalid numeric ID.");
        } catch (ResourceNotFoundException exception) {
            JsonUtil.error(response, 404, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            JsonUtil.error(response, 400, exception.getMessage());
        }
    }
}
