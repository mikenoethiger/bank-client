package bank.websocket;

import bank.*;
import bank.protocol.Request;
import bank.protocol.Response;

import java.io.IOException;
import java.util.*;

public class WsBank implements Bank {

    private final WsConnection connection;
    /* client side account cache to maintain consistency for multiple references to the same account */
    private final Map<String, WsAccount> accountsCache = new HashMap<>();

    public WsBank(WsConnection connection) {
        this.connection = connection;
    }

    @Override
    public Set<String> getAccountNumbers() throws IOException {
        Response response = connection.sendRequestSynchronous(new Request(Request.ACTION_GET_ACCOUNT_NUMBERS, new String[]{}));

        if (!response.ok()) throw new IOException("Remote Error: " + response.getData()[0]);

        return new HashSet<>(Arrays.asList(response.getData()));
    }

    @Override
    public Account getAccount(String number) throws IOException {
        Response response = connection.sendRequestSynchronous(new Request(Request.ACTION_GET_ACCOUNT, new String[]{number}));

        if (!response.ok()) return null;

        WsAccount acc = new WsAccount(connection);
        acc.setNumber(response.getData()[0]);
        acc.setOwner(response.getData()[1]);
        acc.setBalance(Double.valueOf(response.getData()[2]));
        acc.setActive(!"0".equals(response.getData()[3]));

        if (accountsCache.containsKey(number)) {
            WsAccount cached = accountsCache.get(number);
            cached.setNumber(acc.getNumber());
            cached.setOwner(acc.getOwner());
            cached.setBalance(acc.getBalance());
            cached.setActive(acc.isActive());
            acc = cached;
        }

        accountsCache.put(number, acc);

        return acc;
    }

    @Override
    public String createAccount(String owner) throws IOException {
        Response response = connection.sendRequestSynchronous(new Request(Request.ACTION_CREATE_ACCOUNT, new String[]{owner}));

        if (!response.ok()) throw new IOException("Remote Error: " + response.getData()[0]);

        return response.getData()[0];
    }

    @Override
    public boolean closeAccount(String number) throws IOException {
        Response response = connection.sendRequestSynchronous(new Request(Request.ACTION_CLOSE_ACCOUNT, new String[]{number}));
        return response.ok();
    }

    @Override
    public void transfer(Account from, Account to, double amount) throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
        Response response = connection.sendRequestSynchronous(new Request(Request.ACTION_TRANSFER, new String[]{from.getNumber(), to.getNumber(), String.valueOf(amount)}));

        if (!(from instanceof ClientAccount) || !(to instanceof ClientAccount)) throw new IllegalArgumentException("this method is only compatible with ClientAccount instances");

        if (response.getStatusCode() == Response.ERROR_ACCOUNT_OVERDRAW.statusCode) throw new OverdrawException();
        else if (response.getStatusCode() == Response.ERROR_INACTIVE_ACCOUNT.statusCode) throw new InactiveException();
        else if (response.getStatusCode() == Response.ERROR_ILLEGAL_ARGUMENT.statusCode) throw new IllegalArgumentException();

        if (!response.ok()) if (!response.ok()) throw new IOException("Remote Error: " + response.getData()[0]);

        ((ClientAccount) from).setBalance(Double.valueOf(response.getData()[0]));
        ((ClientAccount) to).setBalance(Double.valueOf(response.getData()[1]));
    }
}
