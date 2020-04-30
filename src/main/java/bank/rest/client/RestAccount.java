package bank.rest.client;

import bank.ServerAccount;
import bank.InactiveException;
import bank.OverdrawException;
import bank.rest.server.RestServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class RestAccount extends ServerAccount {

    public RestAccount() { }

    public RestAccount(String owner) {
        super(owner);
    }

    private boolean updateBalance(double oldBalance, double newBalance) throws IOException, InactiveException, OverdrawException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(20))
                .build();


        // curl -i -X PUT http://localhost:8080/bank/accounts/CH5610000000000000000 --data "value=100"
        URI uri = HttpHelper.createURIOrFail(RestServer.SERVER + "/bank/accounts/" + getNumber());
        String body = HttpHelper.formEncode("value", String.valueOf(newBalance));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Etag", String.valueOf(oldBalance))
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = HttpHelper.clientSendOrFail(request, client);
        if (response.statusCode() == 412) { // 412 Precondition Failed
            return false;
        } else if (response.statusCode() == 410) { // 410 Gone
            throw new InactiveException();
        } else if (response.statusCode() == 400) { // 400 Bad SocketRequest
            throw new OverdrawException();
        }
        else if (response.statusCode() != 200) {
            throw new IOException(response.toString());
        }
        setBalance(newBalance);
        return true;
    }

    @Override
    public void deposit(double amount) throws InactiveException, IOException {
        if (amount < 0) throw new IllegalArgumentException();
        try {
            while (!updateBalance(getBalance(), getBalance()+amount)) {
                fetchBalance();
            }
        } catch (OverdrawException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void withdraw(double amount) throws InactiveException, OverdrawException, IOException {
        if (amount < 0) throw new IllegalArgumentException();
        while (!updateBalance(getBalance(), getBalance()-amount)) {
            fetchBalance();
        }
    }

    private void fetchBalance() throws IOException {
        RestBank bank = new RestBank();
        double balance = bank.getAccount(getNumber()).getBalance();
        setBalance(balance);
    }
}
