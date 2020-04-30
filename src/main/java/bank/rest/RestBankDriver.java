package bank.rest;

import bank.Bank;
import bank.BankDriver;
import bank.rest.client.RestBank;

import java.io.IOException;

public class RestBankDriver implements BankDriver {

    private Bank bank;

    @Override
    public void connect(String[] strings) throws IOException {
        bank = new RestBank();
        System.out.println("connected...");
    }

    @Override
    public void disconnect() throws IOException {
        bank = null;
        System.out.println("disconnected...");
    }

    @Override
    public Bank getBank() {
        return bank;
    }
}
