package org.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.ejb.NoSuchEJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "Quiz", urlPatterns = "/Quiz")
public class Quiz extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String WIZARD_ATTRIBUTE = "questionWizard";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        if (request.getParameter("resetQuiz") != null) {
            QuestionWizard wizard = (QuestionWizard) session.getAttribute(WIZARD_ATTRIBUTE);
            if (wizard != null) {
                try {
                    wizard.reset();
                } catch (NoSuchEJBException ignored) {
                    // The EJB container has already removed an inactive wizard.
                }
            }
            session.removeAttribute(WIZARD_ATTRIBUTE);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        writeWizardState(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String answer = request.getParameter("answer");
        if (answer == null || answer.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Липсва отговор.");
            return;
        }

        HttpSession session = request.getSession();
        QuestionWizard wizard = getOrCreateWizard(session);
        try {
            wizard.answer(answer.trim());
        } catch (NoSuchEJBException ex) {
            // Its five-second EJB timeout expired: start with a fresh wizard.
            session.removeAttribute(WIZARD_ATTRIBUTE);
            wizard = getOrCreateWizard(session);
            wizard.answer(answer.trim());
        }
        writeJson(response, wizard);
    }

    private void writeWizardState(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        QuestionWizard wizard = getOrCreateWizard(session);
        try {
            writeJson(response, wizard);
        } catch (NoSuchEJBException ex) {
            session.removeAttribute(WIZARD_ATTRIBUTE);
            writeJson(response, getOrCreateWizard(session));
        }
    }

    private QuestionWizard getOrCreateWizard(HttpSession session) throws ServletException {
        QuestionWizard wizard = (QuestionWizard) session.getAttribute(WIZARD_ATTRIBUTE);
        if (wizard == null) {
            wizard = createWizard();
            session.setAttribute(WIZARD_ATTRIBUTE, wizard);
        }
        return wizard;
    }

    private QuestionWizard createWizard() throws ServletException {
        try {
            // Each JNDI lookup creates a new stateful EJB for the current HTTP session.
            return (QuestionWizard) new InitialContext().lookup("java:module/QuestionWizard");
        } catch (NamingException ex) {
            // Jetty is a Servlet container, not an EJB container. This fallback keeps
            // the web UI usable with jetty:run; deploy the WAR to WildFly/Payara to
            // observe @StatefulTimeout and @PreDestroy managed by an EJB container.
            return new QuestionWizard();
        }
    }

    private void writeJson(HttpServletResponse response, QuestionWizard wizard) throws IOException {
        // Read the complete EJB state before committing the HTTP response. If the
        // bean timed out, the caller can still replace it and generate a clean JSON response.
        boolean finished = wizard.isFinished();
        int currentNumber = wizard.getCurrentNumber();
        int questionCount = wizard.getQuestionCount();
        String question = wizard.getCurrentQuestion();
        List<String> answers = wizard.getAnswers();

        StringBuilder json = new StringBuilder();
        json.append("{\"finished\":").append(finished);
        json.append(",\"currentNumber\":").append(currentNumber);
        json.append(",\"questionCount\":").append(questionCount);
        json.append(",\"question\":");
        appendJsonString(json, question);
        json.append(",\"answers\":[");
        for (int i = 0; i < answers.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            appendJsonString(json, answers.get(i));
        }
        json.append("]}");

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            out.print(json);
        }
    }

    private void appendJsonString(StringBuilder json, String value) {
        if (value == null) {
            json.append("null");
            return;
        }
        json.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"': json.append("\\\""); break;
                case '\\': json.append("\\\\"); break;
                case '\n': json.append("\\n"); break;
                case '\r': json.append("\\r"); break;
                case '\t': json.append("\\t"); break;
                default: json.append(ch);
            }
        }
        json.append('"');
    }
}
