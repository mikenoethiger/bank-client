package bank.rest.client;

import bank.rest.client.RestBank;

import java.io.IOException;

public class RestBankTest {

    public static void main(String[] args) throws IOException {
        RestBank restBank = new RestBank();
        String account = restBank.createAccount("Mike");
        System.out.println("account: " + account);

        System.out.println("Accounts: " + restBank.getAccountNumbers());

        System.out.println("account: " + restBank.getAccount("CH5610000000000000006"));
    }
}
