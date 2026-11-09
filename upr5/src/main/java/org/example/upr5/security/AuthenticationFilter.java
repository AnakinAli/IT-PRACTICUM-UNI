package org.example.upr5.security;

import io.jsonwebtoken.JwtException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    private final JwtService jwtService = new JwtService();

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        try {
            String header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            String token = BearerToken.fromAuthorizationHeader(header);
            jwtService.validate(token, JwtService.AUTH_TOKEN);
        } catch (JwtException | IllegalArgumentException exception) {
            requestContext.abortWith(UnauthorizedResponse.build());
        }
    }
}
