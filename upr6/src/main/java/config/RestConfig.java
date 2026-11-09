package config;

import org.glassfish.jersey.jackson.JacksonFeature;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/webapi")
public class RestConfig extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(webapi.Authenticate.class);
        classes.add(webapi.FavoriteFruits.class);
        classes.add(JacksonFeature.class);
        return classes;
    }
}
