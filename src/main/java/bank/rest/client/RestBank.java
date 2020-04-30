package bank.rest.client;

import bank.Account;
import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;
import bank.rest.server.RestServer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Dump Traffic on localhost:8080: sudo tcpdump -i lo0 -s0 -A port 8080
 */
public class RestBank implements Bank {

    private final HttpClient client;

    public RestBank() {
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    @Override
    public String createAccount(String s) throws IOException {
        URI uri = HttpHelper.createURIOrFail(RestServer.SERVER + "/bank/accounts");
        String body = HttpHelper.formEncode("owner", s);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = HttpHelper.clientSendOrFail(request, client, 201);

        String[] split = response.headers().firstValue("location").get().split("/");
        return split[split.length - 1];
    }

    @Override
    public boolean closeAccount(String s) throws IOException {
        URI uri = HttpHelper.createURIOrFail(RestServer.SERVER + "/bank/accounts/" + s);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();

        HttpResponse<String> response = HttpHelper.clientSendOrFail(request, client);
        if (response.statusCode() == 200) return true;
        return false;
    }

    @Override
    public Set<String> getAccountNumbers() throws IOException {
        URI uri = HttpHelper.createURIOrFail(RestServer.SERVER + "/bank/accounts");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = HttpHelper.clientSendOrFail(request, client, 200);
        ObjectMapper mapper = new ObjectMapper();
        String[] accounts = mapper.readValue(response.body(), String[].class);

        return new HashSet<>(Arrays.asList(accounts));
    }

    @Override
    public Account getAccount(String s) throws IOException {
        URI uri = HttpHelper.createURIOrFail(RestServer.SERVER + "/bank/accounts/" + s);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = HttpHelper.clientSendOrFail(request, client, 200);
        ObjectMapper mapper = new ObjectMapper();
        RestAccount account = mapper.readValue(response.body(), RestAccount.class);

        return account;
    }

    @Override
    public void transfer(Account account, Account account1, double v) throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
        if (!(account instanceof RestAccount) || !(account1 instanceof RestAccount) || v < 0) {
            throw new IllegalArgumentException();
        }
        RestAccount acc1 = (RestAccount) account;
        RestAccount acc2 = (RestAccount) account1;
        acc1.withdraw(v);
        acc2.deposit(v);
    }
}
