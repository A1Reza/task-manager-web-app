package ir.reza.taskmanager.servlet;

import ir.reza.taskmanager.service.SessionService;
import ir.reza.taskmanager.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/visits")
public class VisitServlet extends HttpServlet {

    private SessionService sessionService;

    @Override
    public void init() throws ServletException {
        sessionService = (SessionService) getServletContext()
                .getAttribute("sessionService");
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {

        int visitCount = sessionService.incrementVisitCount(request.getSession());

        JsonUtil.send(response, HttpServletResponse.SC_OK,
                Map.of("visitCount", visitCount));
    }
}