package ir.reza.taskmanager.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.reza.taskmanager.dto.request.ProjectRequestDto;
import ir.reza.taskmanager.exception.ResourceNotFoundException;
import ir.reza.taskmanager.service.ProjectService;
import ir.reza.taskmanager.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/projects")
public class ProjectServlet extends HttpServlet {

    private ProjectService projectService;

    @Override
    public void init() throws ServletException {
        projectService = (ProjectService) getServletContext()
                .getAttribute("projectService");

        if (projectService == null) {
            throw new ServletException("ProjectService is not initialized.");
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        try {
            String idParameter = request.getParameter("id");

            if (idParameter != null) {
                long id = Long.parseLong(idParameter);

                if (id <= 0) {
                    JsonUtil.error(response, 400, "Project ID must be positive.");
                    return;
                }

                JsonUtil.send(response, 200, projectService.findById(id));
                return;
            }

            JsonUtil.send(response, 200, projectService.findAll());

        } catch (NumberFormatException exception) {
            JsonUtil.error(response, 400, "Invalid project ID.");
        } catch (ResourceNotFoundException exception) {
            JsonUtil.error(response, 404, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            JsonUtil.error(response, 400, exception.getMessage());
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            ProjectRequestDto dto = JsonUtil.read(
                    request.getReader().lines()
                            .collect(java.util.stream.Collectors.joining("\n")),
                    ProjectRequestDto.class
            );

            if (dto.name() == null || dto.name().isBlank()) {
                JsonUtil.error(response, 400, "Project name is required.");
                return;
            }

            JsonUtil.send(response, 201, projectService.add(dto));

        } catch (JsonProcessingException exception) {
            JsonUtil.error(response, 400, "Invalid JSON request body.");
        } catch (IllegalArgumentException exception) {
            JsonUtil.error(response, 400, exception.getMessage());
        }
    }
}
