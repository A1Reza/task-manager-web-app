package ir.reza.taskmanager.filter;

import ir.reza.taskmanager.service.LogService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public class LoggingFilter implements Filter {

    private LogService logService;
    private String logMessage;

    @Override
    public void init(FilterConfig filterConfig) {
        logService = (LogService) filterConfig
                .getServletContext()
                .getAttribute("logService");

        logMessage = filterConfig.getInitParameter("logMessage");

        if (logMessage == null || logMessage.isBlank()) {
            logMessage = "Incoming request";
        }
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest) {
            String message = logMessage
                    + " | Method: " + httpRequest.getMethod()
                    + " | URI: " + httpRequest.getRequestURI();

            if (logService != null) {
                logService.log(message);
            }
        }

        chain.doFilter(request, response);
    }
}
