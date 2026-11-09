package listeners;

import dao.QuestionWizard;

import javax.ejb.embeddable.EJBContainer;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/** Starts the EJB container that Jetty itself does not provide. */
public class OpenEjbContextListener implements ServletContextListener {
    public static final String EJB_CONTAINER_ATTRIBUTE = EJBContainer.class.getName();

    @Override
    public void contextInitialized(ServletContextEvent event) {
        Thread thread = Thread.currentThread();
        ClassLoader oldClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(getClass().getClassLoader());

        try {
            Map<String, Object> properties = new HashMap<>();
            properties.put(EJBContainer.MODULES, classesDirectory());
            // This exercise has no authentication or role checks. Using OpenEJB's
            // no-op security service also keeps the embedded EJB container
            // compatible with JDK 26, where java.security.Policy was removed.
            properties.put("Default Security Service",
                    "new://SecurityService?provider=PseudoSecurityService");
            EJBContainer container = EJBContainer.createEJBContainer(properties);
            event.getServletContext().setAttribute(EJB_CONTAINER_ATTRIBUTE, container);
        } catch (RuntimeException exception) {
            event.getServletContext().log("OpenEJB could not be started", exception);
            throw exception;
        } finally {
            thread.setContextClassLoader(oldClassLoader);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Object value = event.getServletContext().getAttribute(EJB_CONTAINER_ATTRIBUTE);
        if (value instanceof EJBContainer) {
            ((EJBContainer) value).close();
        }
    }

    private File classesDirectory() {
        try {
            return new File(QuestionWizard.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Cannot locate WEB-INF/classes", exception);
        }
    }
}
