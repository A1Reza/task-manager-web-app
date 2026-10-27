
package ir.reza.taskmanager.servlet;

import ir.reza.taskmanager.dto.request.UserRequestDto;
import ir.reza.taskmanager.dto.response.UserResponseDto;
import ir.reza.taskmanager.exception.DuplicateResourceException;
import ir.reza.taskmanager.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

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

        response.setContentType("text/html;charset=UTF-8");

        List<UserResponseDto> users = userService.findAll();

        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Users</title>");
        out.println("</head>");
        out.println("<body>");

        out.println("<h1>Users</h1>");

        out.println("<table border='1' cellpadding='8'>");
        out.println("<tr>");
        out.println("<th>ID</th>");
        out.println("<th>Name</th>");
        out.println("<th>Email</th>");
        out.println("</tr>");

        for (UserResponseDto user : users) {
            out.println("<tr>");
            out.println("<td>" + user.id() + "</td>");
            out.println("<td>" + escapeHtml(user.name()) + "</td>");
            out.println("<td>" + escapeHtml(user.email()) + "</td>");
            out.println("</tr>");
        }

        out.println("</table>");

        out.println("<h2>Add User</h2>");
        out.println("<form method='post' action='"
                + request.getContextPath() + "/users'>");

        out.println("<label>Name:</label>");
        out.println("<input type='text' name='name' required>");
        out.println("<br><br>");

        out.println("<label>Email:</label>");
        out.println("<input type='email' name='email' required>");
        out.println("<br><br>");

        out.println("<button type='submit'>Add User</button>");
        out.println("</form>");

        out.println("<br><a href='"
                + request.getContextPath()
                + "/tasks'>Back to Tasks</a>");

        out.println("</body>");
        out.println("</html>");
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        request.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String email = request.getParameter("email");

        if (name == null || name.isBlank()
                || email == null || email.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Name and email are required."
            );
            return;
        }

        try {
            UserRequestDto dto = new UserRequestDto(
                    name.trim(),
                    email.trim()
            );

            userService.add(dto);

            response.sendRedirect(
                    request.getContextPath() + "/users"
            );

        } catch (DuplicateResourceException exception) {
            response.setStatus(
                    HttpServletResponse.SC_CONFLICT
            );
            response.setContentType("text/html;charset=UTF-8");

            response.getWriter().println(
                    "<h1>Email already exists</h1>"
            );

            response.getWriter().println(
                    "<a href='" + request.getContextPath()
                            + "/users'>Back to Users</a>"
            );
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
                .replace("'", "&#39;");
    }
}
