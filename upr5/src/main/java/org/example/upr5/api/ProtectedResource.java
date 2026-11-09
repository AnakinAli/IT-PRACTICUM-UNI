package org.example.upr5.api;

import org.example.upr5.security.Secured;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("/protected")
@Secured
@Produces(MediaType.APPLICATION_JSON)
public class ProtectedResource {
    @GET
    public Map<String, String> getProtectedValue() {
        return Map.of("message", "JWT authentication succeeded");
    }
}
