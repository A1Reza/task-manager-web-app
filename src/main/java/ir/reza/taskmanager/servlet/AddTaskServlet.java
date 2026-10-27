package ir.reza.taskmanager.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.reza.taskmanager.dto.request.TaskRequestDto;
import ir.reza.taskmanager.exception.InvalidReferenceException;
import ir.reza.taskmanager.exception.ResourceNotFoundException;
import ir.reza.taskmanager.service.TaskService;
import ir.reza.taskmanager.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/tasks/add")
public class AddTaskServlet extends HttpServlet {

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
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            TaskRequestDto dto = JsonUtil.read(
                    request.getReader().lines()
                            .collect(java.util.stream.Collectors.joining("\n")),
                    TaskRequestDto.class
            );

            if (dto.title() == null || dto.title().isBlank()
                    || dto.estimatedMinutes() == null
                    || dto.estimatedMinutes() <= 0) {
                JsonUtil.error(
                        response, 400,
                        "Title and positive estimated minutes are required."
                );
                return;
            }

            JsonUtil.send(response, 201, taskService.add(dto));

        } catch (JsonProcessingException exception) {
            JsonUtil.error(response, 400, "Invalid JSON request body.");
        } catch (InvalidReferenceException exception) {
            JsonUtil.error(response, 400, exception.getMessage());
        } catch (ResourceNotFoundException exception) {
            JsonUtil.error(response, 404, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            JsonUtil.error(response, 400, exception.getMessage());
        }
    }
}
