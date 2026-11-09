package webapi;

import database.Database;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/{username}")
public class CheckUserResource {
    @HEAD
    public Response checkUser(@PathParam("username") String username) {
        return Database.usernameExists(username)
                ? Response.ok().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }
}
