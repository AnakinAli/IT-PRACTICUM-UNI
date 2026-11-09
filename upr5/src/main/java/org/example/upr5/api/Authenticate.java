package org.example.upr5.api;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.example.upr5.security.BearerToken;
import org.example.upr5.security.JwtService;
import org.example.upr5.security.UnauthorizedResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class Authenticate {
    private static final long AUTH_VALIDITY_SECONDS = 300;
    private static final long REFRESH_VALIDITY_SECONDS = 3600;

    private final JwtService jwtService = new JwtService();

    /** Replace this demonstration check with the project's database/service lookup. */
    private boolean credentialsAreValid(LoginRequest request) {
        return request != null
                && "student@example.com".equals(request.getEmail())
                && "student".equals(request.getPassword());
    }

    public String getNewToken(String email, long validitySeconds) {
        return jwtService.createToken(email, validitySeconds, JwtService.AUTH_TOKEN);
    }

    @POST
    @Path("/authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authenticate(LoginRequest request) {
        if (!credentialsAreValid(request)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Invalid email or password"))
                    .build();
        }

        String jwtString = getNewToken(request.getEmail(), AUTH_VALIDITY_SECONDS);
        String refreshToken = jwtService.createToken(
                request.getEmail(), REFRESH_VALIDITY_SECONDS, JwtService.REFRESH_TOKEN);

        HashMap<String, String> tokens = new HashMap<>();
        tokens.put("auth", jwtString);
        tokens.put("refr", refreshToken);
        return Response.ok(tokens).build();
    }

    @POST
    @Path("/refreshAuthToken")
    public Response refreshAuthToken(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization) {
        try {
            String refreshToken = BearerToken.fromAuthorizationHeader(authorization);
            Jws<Claims> claims = jwtService.validate(refreshToken, JwtService.REFRESH_TOKEN);
            String authToken = getNewToken(claims.getBody().getSubject(), AUTH_VALIDITY_SECONDS);

            HashMap<String, String> result = new HashMap<>();
            result.put("auth", authToken);
            return Response.ok(result).build();
        } catch (JwtException | IllegalArgumentException exception) {
            return UnauthorizedResponse.build();
        }
    }
}
