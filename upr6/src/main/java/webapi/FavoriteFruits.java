package webapi;

import io.fusionauth.jwt.JWTExpiredException;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACVerifier;
import privateCryptographicKeys.CryptographicKeys;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/favoriteFruits")
@Produces(MediaType.APPLICATION_JSON)
public class FavoriteFruits {
    public static final String EXPIRED_TOKEN_STATUS = "401";
    public static final String INVALID_TOKEN_STATUS = "403";

    private static final String WWW_AUTHENTICATE_VALUE = "Bearer realm=\"http://localhost\","
            + "error=\"invalid_token\","
            + "error_description=\"The access token is invalid or expired\"";

    private static final HashMap<String, ArrayList<String>> favoriteFruits = new HashMap<>();
    static {
        favoriteFruits.put("ivan@example.com", new ArrayList<>(Arrays.asList("ябълка", "банан")));
        favoriteFruits.put("maria@example.com", new ArrayList<>(Arrays.asList("ягода", "череша")));
        favoriteFruits.put("student@example.com", new ArrayList<>(Arrays.asList("портокал", "круша")));
    }

    @Context private HttpServletRequest request;

    /** Ordinary Java method (not a web service), as required by the exercise. */
    public Response createResponse(String statusCode) {
        int numericStatusCode = Integer.parseInt(statusCode);
        if (numericStatusCode == 401 || numericStatusCode == 403) {
            return Response.status(numericStatusCode)
                    .header("WWW-Authenticate", WWW_AUTHENTICATE_VALUE)
                    .build();
        }
        return Response.status(numericStatusCode).build();
    }

    /**
     * Returns an e-mail for a valid token, "401" for an expired token and
     * "403" for any other invalid token.
     */
    public String validateJWT(String jwtString) {
        if (jwtString == null || jwtString.trim().isEmpty()) return INVALID_TOKEN_STATUS;
        try {
            String unverifiedEmail = JWTSupport.readUnverifiedSubject(jwtString);
            String password = CryptographicKeys.getUserJWTPassword(unverifiedEmail);
            if (password == null) return INVALID_TOKEN_STATUS;

            Verifier verifier = HMACVerifier.newVerifier(password);
            JWT jwt = JWT.getDecoder().decode(jwtString, verifier);
            return unverifiedEmail.equals(jwt.subject) ? jwt.subject : INVALID_TOKEN_STATUS;
        } catch (JWTExpiredException exception) {
            return EXPIRED_TOKEN_STATUS;
        } catch (Exception exception) {
            return INVALID_TOKEN_STATUS;
        }
    }

    @GET
    public Response getFavoriteFruits() {
        String validationResult = authenticatedEmail();
        if (!validationResult.contains("@")) return createResponse(validationResult);
        return Response.ok(copyOfFruits(validationResult)).build();
    }

    @POST @Consumes(MediaType.APPLICATION_JSON)
    public Response addFavoriteFruit(FruitRequest body) {
        String validationResult = authenticatedEmail();
        if (!validationResult.contains("@")) return createResponse(validationResult);
        if (body == null || isBlank(body.getFruit())) return badRequest("Подайте JSON във формат {\"fruit\":\"манго\"}.");
        ArrayList<String> fruits = fruitsFor(validationResult);
        synchronized (fruits) { if (!fruits.contains(body.getFruit())) fruits.add(body.getFruit()); }
        return Response.status(Response.Status.CREATED).entity(copyOfFruits(validationResult)).build();
    }

    @DELETE @Path("/{fruit}")
    public Response deleteFavoriteFruit(@PathParam("fruit") String fruit) {
        String validationResult = authenticatedEmail();
        if (!validationResult.contains("@")) return createResponse(validationResult);
        ArrayList<String> fruits = fruitsFor(validationResult);
        boolean removed;
        synchronized (fruits) { removed = fruits.remove(fruit); }
        if (!removed) return Response.status(Response.Status.NOT_FOUND).entity(error("Плодът не е намерен.")).build();
        return Response.ok(copyOfFruits(validationResult)).build();
    }

    @PUT @Consumes(MediaType.APPLICATION_JSON)
    public Response replaceFavoriteFruit(UpdateFruitRequest body) {
        String validationResult = authenticatedEmail();
        if (!validationResult.contains("@")) return createResponse(validationResult);
        if (body == null || isBlank(body.getOldFruit()) || isBlank(body.getNewFruit()))
            return badRequest("Подайте JSON във формат {\"oldFruit\":\"банан\",\"newFruit\":\"манго\"}.");
        ArrayList<String> fruits = fruitsFor(validationResult);
        synchronized (fruits) {
            int index = fruits.indexOf(body.getOldFruit());
            if (index < 0) return Response.status(Response.Status.NOT_FOUND).entity(error("Плодът за замяна не е намерен.")).build();
            fruits.set(index, body.getNewFruit());
        }
        return Response.ok(copyOfFruits(validationResult)).build();
    }

    private String authenticatedEmail() {
        return validateJWT(JWTSupport.readToken(request));
    }

    private static List<String> copyOfFruits(String email) {
        ArrayList<String> fruits = fruitsFor(email);
        synchronized (fruits) { return new ArrayList<>(fruits); }
    }

    private static synchronized ArrayList<String> fruitsFor(String email) {
        return favoriteFruits.computeIfAbsent(email, ignored -> new ArrayList<>());
    }

    private static Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST).entity(error(message)).build();
    }

    private static Map<String, String> error(String message) {
        return Collections.singletonMap("error", message);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
