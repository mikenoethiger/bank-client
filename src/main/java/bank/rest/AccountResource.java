package bank.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class AccountResource {

    @GET
    public String root() {
        return "Hello World";
    }

}
