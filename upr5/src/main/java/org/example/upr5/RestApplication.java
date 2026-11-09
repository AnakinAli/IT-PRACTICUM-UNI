package org.example.upr5;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class RestApplication extends ResourceConfig {
    public RestApplication() {
        packages("org.example.upr5");
        register(JacksonFeature.class);
    }
}
