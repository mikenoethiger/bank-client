package bank.graphql;

import bank.ClientAccount;
import bank.InactiveException;
import bank.OverdrawException;
import bank.socket.SocketResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;

public class GraphQLAccount extends ClientAccount {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void deposit(double amount) throws IOException, InactiveException {
        JsonObject jsonObject = GraphQLBankDriver.sendRequest(mutationDeposit(getNumber(), amount));
        JsonElement responseElement = jsonObject.get("deposit");
        GraphQLResponse response = gson.fromJson(responseElement, GraphQLResponse.class);

        if (response.getStatusCode() == SocketResponse.ERROR_ILLEGAL_ARGUMENT) throw new IllegalArgumentException();
        else if (response.getStatusCode() == SocketResponse.ERROR_INACTIVE_ACCOUNT) throw new InactiveException();

        setBalance(getBalance() + amount);
    }

    @Override
    public void withdraw(double amount) throws IOException, InactiveException, OverdrawException {
        JsonObject jsonObject = GraphQLBankDriver.sendRequest(mutationWithdraw(getNumber(), amount));
        JsonElement responseElement = jsonObject.get("withdraw");
        GraphQLResponse response = gson.fromJson(responseElement, GraphQLResponse.class);

        if (response.getStatusCode() == SocketResponse.ERROR_ILLEGAL_ARGUMENT) throw new IllegalArgumentException();
        else if (response.getStatusCode() == SocketResponse.ERROR_INACTIVE_ACCOUNT) throw new InactiveException();
        else if (response.getStatusCode() == SocketResponse.ERROR_ACCOUNT_OVERDRAW) throw new OverdrawException();

        setBalance(getBalance() - amount);
    }

    private static String mutationDeposit(String accountNumber, double amount) {
        return String.format("mutation {" +
                "  deposit(accountNumber: \"%s\" amount: %s) {" +
                "    statusCode" +
                "    data" +
                "  }" +
                "}", accountNumber, amount);
    }

    private static String mutationWithdraw(String accountNumber, double amount) {
        return String.format("mutation {" +
                "  withdraw(accountNumber: \"%s\" amount: %s) {" +
                "    statusCode" +
                "    data" +
                "  }" +
                "}", accountNumber, amount);
    }
}
