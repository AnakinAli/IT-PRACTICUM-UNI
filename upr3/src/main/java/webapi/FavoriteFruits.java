package webapi;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/favoriteFruits")
@Produces(MediaType.APPLICATION_JSON)
public class FavoriteFruits {
    private static final HashMap<String, ArrayList<String>> favoriteFruits = new HashMap<>();
    static {
        favoriteFruits.put("ivan@example.com", new ArrayList<>(Arrays.asList("ябълка", "банан")));
        favoriteFruits.put("maria@example.com", new ArrayList<>(Arrays.asList("ягода", "череша")));
        favoriteFruits.put("student@example.com", new ArrayList<>(Arrays.asList("портокал", "круша")));
    }

    @Context private HttpServletRequest request;
    private String validateJWT(String jwtString) { return JWTSupport.validate(jwtString); }

    @GET
    public Response getFavoriteFruits() {
        String email = authenticatedEmail();
        return email == null ? unauthorized() : Response.ok(copyOfFruits(email)).build();
    }

    @POST @Consumes(MediaType.APPLICATION_JSON)
    public Response addFavoriteFruit(FruitRequest body) {
        String email = authenticatedEmail();
        if (email == null) return unauthorized();
        if (body == null || isBlank(body.getFruit())) return badRequest("Подайте JSON във формат {\"fruit\":\"манго\"}.");
        ArrayList<String> fruits = fruitsFor(email);
        synchronized (fruits) { if (!fruits.contains(body.getFruit())) fruits.add(body.getFruit()); }
        return Response.status(Response.Status.CREATED).entity(copyOfFruits(email)).build();
    }

    @DELETE @Path("/{fruit}")
    public Response deleteFavoriteFruit(@PathParam("fruit") String fruit) {
        String email = authenticatedEmail();
        if (email == null) return unauthorized();
        ArrayList<String> fruits = fruitsFor(email);
        boolean removed;
        synchronized (fruits) { removed = fruits.remove(fruit); }
        if (!removed) return Response.status(Response.Status.NOT_FOUND).entity(error("Плодът не е намерен.")).build();
        return Response.ok(copyOfFruits(email)).build();
    }

    @PUT @Consumes(MediaType.APPLICATION_JSON)
    public Response replaceFavoriteFruit(UpdateFruitRequest body) {
        String email = authenticatedEmail();
        if (email == null) return unauthorized();
        if (body == null || isBlank(body.getOldFruit()) || isBlank(body.getNewFruit()))
            return badRequest("Подайте JSON във формат {\"oldFruit\":\"банан\",\"newFruit\":\"манго\"}.");
        ArrayList<String> fruits = fruitsFor(email);
        synchronized (fruits) {
            int index = fruits.indexOf(body.getOldFruit());
            if (index < 0) return Response.status(Response.Status.NOT_FOUND).entity(error("Плодът за замяна не е намерен.")).build();
            fruits.set(index, body.getNewFruit());
        }
        return Response.ok(copyOfFruits(email)).build();
    }

    private String authenticatedEmail() { return validateJWT(JWTSupport.readToken(request)); }
    private static List<String> copyOfFruits(String email) {
        ArrayList<String> fruits = fruitsFor(email);
        synchronized (fruits) { return new ArrayList<>(fruits); }
    }
    private static synchronized ArrayList<String> fruitsFor(String email) {
        return favoriteFruits.computeIfAbsent(email, ignored -> new ArrayList<>());
    }
    private static Response unauthorized() { return Response.status(Response.Status.UNAUTHORIZED).entity(error("Невалиден или изтекъл JWT.")).build(); }
    private static Response badRequest(String message) { return Response.status(Response.Status.BAD_REQUEST).entity(error(message)).build(); }
    private static Map<String, String> error(String message) { return Collections.singletonMap("error", message); }
    private static boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
}
