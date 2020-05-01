package bank.rabbitmq;

import bank.Account;
import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;
import bank.socket.SocketRequest;
import bank.socket.SocketResponse;

import java.io.IOException;
import java.util.*;

public class MqBank implements Bank {

    private final MqConnection mqConnection;
    /* client side account cache to maintain consistency for multiple references to the same account */
    private final Map<String, MqAccount> accountsCache = new HashMap<>();

    public MqBank(MqConnection mqConnection) {
        this.mqConnection = mqConnection;
    }

    @Override
    public String createAccount(String owner) throws IOException {
        MqResponse response = MqBankDriver.sendRequest(new MqRequest(SocketRequest.ACTION_CREATE_ACCOUNT, new String[]{owner}), mqConnection);

        if (!response.ok()) return null;

        MqAccount acc = parseAccount(response.getData());
        accountsCache.put(acc.getNumber(), acc);

        return acc.getNumber();
    }

    @Override
    public boolean closeAccount(String accountNumber) throws IOException {
        MqResponse response = MqBankDriver.sendRequest(new MqRequest(SocketRequest.ACTION_CLOSE_ACCOUNT, new String[]{accountNumber}), mqConnection);
        return response.ok();
    }

    @Override
    public Set<String> getAccountNumbers() throws IOException {
        MqResponse response = MqBankDriver.sendRequest(new MqRequest(SocketRequest.ACTION_GET_ACCOUNT_NUMBERS, new String[]{}), mqConnection);

        if (!response.ok()) return new HashSet<>();

        return new HashSet<>(Arrays.asList(response.getData()));
    }

    @Override
    public Account getAccount(String accountNumber) throws IOException {
        System.out.println("getAccount() called");
        MqResponse response = MqBankDriver.sendRequest(new MqRequest(SocketRequest.ACTION_GET_ACCOUNT, new String[]{accountNumber}), mqConnection);

        if (!response.ok()) return null;

        MqAccount acc = parseAccount(response.getData());
        if (accountsCache.containsKey(accountNumber)) {
            MqAccount cached = accountsCache.get(accountNumber);
            cached.setNumber(acc.getNumber());
            cached.setOwner(acc.getOwner());
            cached.setActive(acc.isActive());
            cached.setBalance(acc.getBalance());
            acc = cached;
        }
        accountsCache.put(accountNumber, acc);

        System.out.println("Fetched account: " + acc);
        return acc;
    }

    @Override
    public void transfer(Account from, Account to, double amount) throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
        if (!(from instanceof MqAccount) || !(to instanceof MqAccount)) throw new IllegalArgumentException("this method is only compatible with MqAccount instances");

        MqResponse response = MqBankDriver.sendRequest(new MqRequest(SocketRequest.ACTION_TRANSFER, new String[]{from.getNumber(), to.getNumber(), String.valueOf(amount)}), mqConnection);

        if (response.getStatusCode() == SocketResponse.ERROR_ACCOUNT_OVERDRAW) throw new OverdrawException();
        else if (response.getStatusCode() == SocketResponse.ERROR_INACTIVE_ACCOUNT) throw new InactiveException();
        else if (response.getStatusCode() == SocketResponse.ERROR_ILLEGAL_ARGUMENT) throw new IllegalArgumentException();

        ((MqAccount) from).setBalance(Double.valueOf(response.getData()[0]));
        ((MqAccount) to).setBalance(Double.valueOf(response.getData()[1]));
    }

    private MqAccount parseAccount(String[] accountData) {
        MqAccount acc = new MqAccount(mqConnection);
        acc.setNumber(accountData[0]);
        acc.setOwner(accountData[1]);
        acc.setBalance(Double.parseDouble(accountData[2]));
        acc.setActive(Integer.valueOf(accountData[3]) != 0);
        return acc;
    }
}
