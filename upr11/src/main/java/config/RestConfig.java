package config;

import java.util.Collections;
import java.util.Set;
import javax.ws.rs.core.Application;
import webapi.CheckUserResource;

public class RestConfig extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Collections.<Class<?>>singleton(CheckUserResource.class);
    }
}
