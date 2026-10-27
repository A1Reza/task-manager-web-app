package ir.reza.taskmanager.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.reza.taskmanager.dto.request.UserRequestDto;
import ir.reza.taskmanager.exception.DuplicateResourceException;
import ir.reza.taskmanager.exception.ResourceNotFoundException;
import ir.reza.taskmanager.service.UserService;
import ir.reza.taskmanager.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/users")
public class UserServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() throws ServletException {
        userService = (UserService) getServletContext()
                .getAttribute("userService");

        if (userService == null) {
            throw new ServletException("UserService is not initialized.");
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
                    JsonUtil.error(response, 400, "User ID must be positive.");
                    return;
                }

                JsonUtil.send(response, 200, userService.findById(id));
                return;
            }

            JsonUtil.send(response, 200, userService.findAll());

        } catch (NumberFormatException exception) {
            JsonUtil.error(response, 400, "Invalid user ID.");
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
            UserRequestDto dto = JsonUtil.read(
                    request.getReader().lines()
                            .collect(java.util.stream.Collectors.joining("\n")),
                    UserRequestDto.class
            );

            if (dto.name() == null || dto.name().isBlank()
                    || dto.email() == null || dto.email().isBlank()) {
                JsonUtil.error(response, 400, "Name and email are required.");
                return;
            }

            JsonUtil.send(response, 201, userService.add(dto));

        } catch (JsonProcessingException exception) {
            JsonUtil.error(response, 400, "Invalid JSON request body.");
        } catch (DuplicateResourceException exception) {
            JsonUtil.error(response, 409, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            JsonUtil.error(response, 400, exception.getMessage());
        }
    }
}
