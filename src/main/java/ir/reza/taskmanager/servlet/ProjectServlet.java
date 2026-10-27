package ir.reza.taskmanager.servlet;

import ir.reza.taskmanager.dto.request.ProjectRequestDto;
import ir.reza.taskmanager.dto.response.ProjectResponseDto;
import ir.reza.taskmanager.service.ProjectService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/projects")
public class ProjectServlet extends HttpServlet {

    private ProjectService projectService;

    @Override
    public void init() throws ServletException {
        projectService = (ProjectService) getServletContext()
                .getAttribute("projectService");

        if (projectService == null) {
            throw new ServletException(
                    "ProjectService is not initialized."
            );
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        List<ProjectResponseDto> projects = projectService.findAll();

        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Projects</title>");
        out.println("</head>");
        out.println("<body>");

        out.println("<h1>Projects</h1>");

        out.println("<table border='1' cellpadding='8'>");
        out.println("<tr>");
        out.println("<th>ID</th>");
        out.println("<th>Name</th>");
        out.println("<th>Description</th>");
        out.println("</tr>");

        for (ProjectResponseDto project : projects) {
            out.println("<tr>");
            out.println("<td>" + project.id() + "</td>");
            out.println("<td>" + escapeHtml(project.name()) + "</td>");
            out.println("<td>" + escapeHtml(project.description()) + "</td>");
            out.println("</tr>");
        }

        out.println("</table>");

        out.println("<h2>Add Project</h2>");

        out.println("<form method='post' action='"
                + request.getContextPath() + "/projects'>");

        out.println("<label>Name:</label>");
        out.println("<input type='text' name='name' required>");
        out.println("<br><br>");

        out.println("<label>Description:</label>");
        out.println("<textarea name='description' rows='4' cols='40'></textarea>");
        out.println("<br><br>");

        out.println("<button type='submit'>Add Project</button>");
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
        String description = request.getParameter("description");

        if (name == null || name.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Project name is required."
            );
            return;
        }

        try {
            ProjectRequestDto dto = new ProjectRequestDto(
                    name.trim(),
                    description == null || description.isBlank()
                            ? null
                            : description.trim()
            );

            projectService.add(dto);

            response.sendRedirect(
                    request.getContextPath() + "/projects"
            );

        } catch (IllegalArgumentException exception) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid project data."
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
