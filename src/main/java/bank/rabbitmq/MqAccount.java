package bank.rabbitmq;

import bank.ClientAccount;
import bank.InactiveException;
import bank.OverdrawException;
import bank.socket.SocketRequest;
import bank.socket.SocketResponse;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

public class MqAccount extends ClientAccount {

    private final ConnectionFactory factory;

    public MqAccount(ConnectionFactory factory) {
        this.factory = factory;
    }

    @Override
    public void deposit(double amount) throws IOException, IllegalArgumentException, InactiveException {
        MqResponse response = MqBankDriver.sendRequest(new MqRequest(SocketRequest.ACTION_DEPOSIT, new String[]{getNumber(), String.valueOf(amount)}), factory);

        if (response.getStatusCode() == SocketResponse.ERROR_INACTIVE_ACCOUNT) throw new InactiveException();
        else if (response.getStatusCode() == SocketResponse.ERROR_ILLEGAL_ARGUMENT) throw new IllegalArgumentException();
        else if (response.getStatusCode() != SocketResponse.OK_STATUS_CODE) throw new IOException(response.getData()[0]);

        setBalance(getBalance() + amount);
    }

    @Override
    public void withdraw(double amount) throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
        MqResponse response = MqBankDriver.sendRequest(new MqRequest(SocketRequest.ACTION_WITHDRAW, new String[]{getNumber(), String.valueOf(amount)}), factory);

        if (response.getStatusCode() == SocketResponse.ERROR_INACTIVE_ACCOUNT) throw new InactiveException();
        else if (response.getStatusCode() == SocketResponse.ERROR_ACCOUNT_OVERDRAW) throw new OverdrawException();
        else if (response.getStatusCode() == SocketResponse.ERROR_ILLEGAL_ARGUMENT) throw new IllegalArgumentException();
        else if (response.getStatusCode() != SocketResponse.OK_STATUS_CODE) throw new IOException(response.getData()[0]);

        setBalance(getBalance() - amount);
    }
}
