package bank.rabbitmq;

import bank.InactiveException;
import bank.OverdrawException;
import bank.protocol.DefaultAccount;
import bank.protocol.Request;
import bank.protocol.Response;
import bank.socket.SocketRequest;
import bank.socket.SocketResponse;

import java.io.IOException;

public class MqAccount extends DefaultAccount {

    private final MqConnection mqConnection;

    public MqAccount(MqConnection mqConnection) {
        this.mqConnection = mqConnection;
    }

    @Override
    public void deposit(double amount) throws IOException, IllegalArgumentException, InactiveException {
        Response response = MqBankDriver.sendRequest(new Request(SocketRequest.ACTION_DEPOSIT, new String[]{getNumber(), String.valueOf(amount)}), mqConnection);

        if (response.getStatusCode() == SocketResponse.ERROR_INACTIVE_ACCOUNT) throw new InactiveException();
        else if (response.getStatusCode() == SocketResponse.ERROR_ILLEGAL_ARGUMENT) throw new IllegalArgumentException();
        else if (response.getStatusCode() != SocketResponse.OK_STATUS_CODE) throw new IOException(response.getData()[0]);

        setBalance(getBalance() + amount);
    }

    @Override
    public void withdraw(double amount) throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
        Response response = MqBankDriver.sendRequest(new Request(SocketRequest.ACTION_WITHDRAW, new String[]{getNumber(), String.valueOf(amount)}), mqConnection);

        if (response.getStatusCode() == SocketResponse.ERROR_INACTIVE_ACCOUNT) throw new InactiveException();
        else if (response.getStatusCode() == SocketResponse.ERROR_ACCOUNT_OVERDRAW) throw new OverdrawException();
        else if (response.getStatusCode() == SocketResponse.ERROR_ILLEGAL_ARGUMENT) throw new IllegalArgumentException();
        else if (response.getStatusCode() != SocketResponse.OK_STATUS_CODE) throw new IOException(response.getData()[0]);

        setBalance(getBalance() - amount);
    }
}
