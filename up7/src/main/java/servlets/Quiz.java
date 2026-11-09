package servlets;

import dao.QuestionWizard;
import listeners.OpenEjbContextListener;

import javax.ejb.NoSuchEJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Properties;

@WebServlet(name = "Quiz", urlPatterns = "/Quiz")
public class Quiz extends HttpServlet {
    private static final String WIZARD_SESSION_ATTRIBUTE = "questionWizard";
    private static final String WIZARD_JNDI_NAME = "java:global/up7/QuestionWizard";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        configureResponse(response);
        HttpSession session = request.getSession();
        QuestionWizard questionWizard =
                (QuestionWizard) session.getAttribute(WIZARD_SESSION_ATTRIBUTE);

        if (questionWizard == null) {
            questionWizard = createQuestionWizard(session);
        }
        if (questionWizard == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "EJB обектът не може да бъде създаден.");
            return;
        }

        String nextQuestion;
        try {
            // Calling this twice as a proxy health check would skip a question.
            nextQuestion = questionWizard.getNextQestion();
        } catch (NoSuchEJBException exception) {
            session.removeAttribute(WIZARD_SESSION_ATTRIBUTE);
            questionWizard = createQuestionWizard(session);
            if (questionWizard == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "EJB обектът не може да бъде възстановен.");
                return;
            }
            nextQuestion = questionWizard.getNextQestion();
        }

        if (nextQuestion == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.getWriter().print(nextQuestion);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        configureResponse(response);
        HttpSession session = request.getSession();
        QuestionWizard questionWizard =
                (QuestionWizard) session.getAttribute(WIZARD_SESSION_ATTRIBUTE);

        if ("true".equals(request.getParameter("completed"))) {
            if (questionWizard == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Сесията на въпросника е изтекла.");
                return;
            }
            questionWizard.sendAnswersToDatabase();
            session.removeAttribute(WIZARD_SESSION_ATTRIBUTE);
            response.getWriter().print("Отговорите са записани успешно в базата от данни.");
            return;
        }

        if (questionWizard == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Сесията на въпросника е изтекла.");
            return;
        }

        questionWizard.saveAnswer(request.getParameter("answer"));
        response.sendRedirect(request.getContextPath() + "/index.html");
    }

    private QuestionWizard createQuestionWizard(HttpSession session) {
        Object containerObject = getServletContext()
                .getAttribute(OpenEjbContextListener.EJB_CONTAINER_ATTRIBUTE);
        if (!(containerObject instanceof EJBContainer)) {
            return null;
        }

        InitialContext context = null;
        try {
            Properties properties = new Properties();
            properties.put(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.openejb.core.OpenEJBInitialContextFactory");
            context = new InitialContext(properties);
            QuestionWizard wizard = (QuestionWizard) context.lookup(WIZARD_JNDI_NAME);
            session.setAttribute(WIZARD_SESSION_ATTRIBUTE, wizard);
            return wizard;
        } catch (NamingException exception) {
            getServletContext().log("Cannot find " + WIZARD_JNDI_NAME, exception);
            return null;
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException exception) {
                    getServletContext().log("Cannot close the OpenEJB JNDI context", exception);
                }
            }
        }
    }

    private void configureResponse(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
    }
}
