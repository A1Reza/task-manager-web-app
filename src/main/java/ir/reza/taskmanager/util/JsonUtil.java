package ir.reza.taskmanager.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtil() {
    }

    public static <T> T read(
            String json,
            Class<T> type
    ) throws JsonProcessingException {
        return MAPPER.readValue(json, type);
    }

    public static void send(
            HttpServletResponse response,
            int status,
            Object data
    ) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        MAPPER.writeValue(response.getWriter(), data);
    }

    public static void error(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {
        send(response, status, Map.of("message", message));
    }
}
