package webapi;

import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACSigner;
import privateCryptographicKeys.CryptographicKeys;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;

@Path("/authenticate")
public class Authenticate {
    private static final HashMap<String, String> users = new HashMap<>();
    static {
        users.put("ivan@example.com", "ivan123");
        users.put("maria@example.com", "maria123");
        users.put("student@example.com", "student123");
    }

    @Context private HttpServletRequest request;

    @GET @Produces(MediaType.TEXT_PLAIN)
    public Response authenticate() {
        return authenticateUser(request.getHeader("email"), request.getHeader("password"));
    }

    @POST @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.TEXT_PLAIN)
    public Response authenticate(LoginRequest login) {
        if (login == null) return Response.status(Response.Status.BAD_REQUEST).entity("Липсват данни за вход.").build();
        return authenticateUser(login.getEmail(), login.getPassword());
    }

    @POST @Path("/logout") @Produces(MediaType.TEXT_PLAIN)
    public Response logout() {
        String userEmail = JWTSupport.validate(JWTSupport.readToken(request));
        if (userEmail == null) return Response.status(Response.Status.UNAUTHORIZED).entity("Невалиден или изтекъл JWT.").build();
        CryptographicKeys.changeUserJWTPassword(userEmail);
        return Response.ok("Успешен logout.").build();
    }

    private Response authenticateUser(String email, String password) {
        if (email != null && password != null && password.equals(users.get(email)))
            return Response.ok(getNewToken(email)).build();
        return Response.status(Response.Status.NOT_FOUND).entity("Невалиден имейл или парола.").build();
    }

    private String getNewToken(String userEmail) {
        Signer signer = HMACSigner.newSHA512Signer(CryptographicKeys.getUserJWTPassword(userEmail));
        JWT jwt = new JWT().setIssuer("http://localhost").setIssuedAt(ZonedDateTime.now(ZoneOffset.UTC))
                .setSubject(userEmail).setExpiration(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(60));
        return JWT.getEncoder().encode(jwt, signer);
    }
}
