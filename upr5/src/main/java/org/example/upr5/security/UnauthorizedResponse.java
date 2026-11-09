package org.example.upr5.security;

import javax.ws.rs.core.Response;
import java.util.Collections;

public final class UnauthorizedResponse {
    private static final String CHALLENGE = "Bearer realm=\"http://localhost\","
            + "error=\"invalid_token\","
            + "error_description=\"The access token is invalid or expired\"";

    private UnauthorizedResponse() {
    }

    public static Response build() {
        return Response.status(Response.Status.UNAUTHORIZED)
                .header("WWW-Authenticate", CHALLENGE)
                .entity(Collections.singletonMap("error", "Invalid or expired token"))
                .build();
    }
}
