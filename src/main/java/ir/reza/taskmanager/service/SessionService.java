package ir.reza.taskmanager.service;

import jakarta.servlet.http.HttpSession;

public class SessionService {

    private static final String VISIT_COUNT = "visitCount";

    public int incrementVisitCount(HttpSession session) {
        Integer count = (Integer) session.getAttribute(VISIT_COUNT);

        if (count == null) {
            count = 0;
        }

        count++;

        session.setAttribute(VISIT_COUNT, count);

        return count;
    }
}