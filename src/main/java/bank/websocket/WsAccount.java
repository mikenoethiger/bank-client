package bank.websocket;

import bank.ClientAccount;
import bank.InactiveException;
import bank.OverdrawException;
import bank.protocol.Request;
import bank.protocol.Response;

import java.io.IOException;

public class WsAccount extends ClientAccount {

    private final WsConnection connection;

    public WsAccount(WsConnection connection) {
        this.connection = connection;
    }

    @Override
    public void deposit(double amount) throws IOException, IllegalArgumentException, InactiveException {
        Response response = connection.sendRequestSynchronous(new Request(Request.ACTION_DEPOSIT, new String[]{getNumber(), String.valueOf(amount)}));

        if (response.getStatusCode() == Response.ERROR_INACTIVE_ACCOUNT.statusCode) throw new InactiveException();
        else if (response.getStatusCode() == Response.ERROR_ILLEGAL_ARGUMENT.statusCode) throw new IllegalArgumentException();
        else if (response.getStatusCode() != Response.STATUS_OK) throw new IOException(response.getData()[0]);

        setBalance(getBalance() + amount);
    }

    @Override
    public void withdraw(double amount) throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
        Response response = connection.sendRequestSynchronous(new Request(Request.ACTION_WITHDRAW, new String[]{getNumber(), String.valueOf(amount)}));

        if (response.getStatusCode() == Response.ERROR_INACTIVE_ACCOUNT.statusCode) throw new InactiveException();
        else if (response.getStatusCode() == Response.ERROR_ACCOUNT_OVERDRAW.statusCode) throw new OverdrawException();
        else if (response.getStatusCode() == Response.ERROR_ILLEGAL_ARGUMENT.statusCode) throw new IllegalArgumentException();
        else if (response.getStatusCode() != Response.STATUS_OK) throw new IOException(response.getData()[0]);

        setBalance(getBalance() - amount);
    }
}
