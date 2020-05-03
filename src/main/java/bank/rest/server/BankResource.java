package bank.rest.server;

import bank.Account;
import bank.Bank;
import bank.protocol.DefaultAccount;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * CURL QUERIES:
 * Create Account:     curl -i -X POST http://localhost:8080/bank/accounts --data "owner=mike"
 * Get Accounts:       curl -i http://localhost:8080/bank/accounts
 * Get Account:        curl -i http://localhost:8080/bank/accounts/CH5610000000000000000
 * Update Balance:     curl -i -X PUT http://localhost:8080/bank/accounts/CH5610000000000000000 --data "value=100"
 * Close Account:      curl -i -X DELETE http://localhost:8080/bank/accounts/CH5610000000000000000
 * Account Status:     curl -i -X HEAD http://localhost:8080/bank/accounts/CH5610000000000000000
 */
@Path("/bank/accounts")
public class BankResource {

    private final Bank bank = RestServer.BANK;

    @GET
    @Produces("application/json")
    public Set<String> getAccountNumbers() throws IOException {
        return bank.getAccountNumbers();
    }

    @POST
    @Produces("application/json")
    public Response createAccount(@FormParam("owner") String owner) throws IOException, URISyntaxException {
        System.out.println(owner);
        String account = bank.createAccount(owner);
        return Response.created(new URI("/bank/accounts/" + account)).build();
    }

    @GET
    @Path("{account}")
    @Produces("application/json")
    public Response getAccount(@PathParam("account") String account) throws IOException {
        Account a = bank.getAccount(account);
        if (a == null) {
            return Response.status(404, "Account not found.").build();
        }
        return Response.ok(bank.getAccount(account)).build();
    }

    @PUT
    @Path("{account}")
    public Response putSalod(@PathParam("account") String account, @FormParam("value") double value) throws IOException {
        DefaultAccount a = (DefaultAccount) bank.getAccount(account);
        if (a == null) {
            return Response.status(404, "Account not found.").build();
        }
        if (!a.isActive()) {
            return Response.status(410, "Account closed.").build();
        }
        if (value < 0) {
            return Response.status(400, "Negative value not allowed.").build();
        }
        a.setBalance(value);
        return Response.ok().build();
    }

    @DELETE
    @Path("{account}")
    public Response closeAccount(@PathParam("account") String account) throws IOException {
        boolean result = bank.closeAccount(account);
        if (result) {
            return Response.ok().build();
        } else {
            return Response.status(400, "Couldn't close account.").build();
        }
    }

    @HEAD
    @Path("{account}")
    public Response accountStatus(@PathParam("account") String account) throws IOException {
        Account a = bank.getAccount(account);
        if (a == null) return Response.status(404).build();
        if (!a.isActive()) return Response.status(410).build();
        return Response.ok().build();
    }

}
