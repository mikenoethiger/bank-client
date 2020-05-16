package bank.graphql;

import bank.Account;
import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;
import bank.socket.SocketResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.*;

public class GraphQLBank implements Bank {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    /* client side account cache to maintain consistency for multiple references to the same account */
    // XXX Problem: Die Einträge werden nie mehr gelöscht. Da müsste man etwas a la WeakHashMap, bei der 
    //     Einträge automatisch gelöscht werden wenn der Key nicht mehr referenziert wird.
    private final Map<String, GraphQLAccount> accountsCache = new HashMap<>();

    @Override
    public Set<String> getAccountNumbers() throws IOException {
        JsonObject jsonObject = GraphQLBankDriver.sendRequest(queryAccounts());

        JsonElement jsonAccounts = jsonObject.get("accounts");
        String[] accs = gson.fromJson(jsonAccounts, String[].class);

        return new HashSet<>(Arrays.asList(accs));
    }

    @Override
    public Account getAccount(String number) throws IOException {
        if (accountsCache.containsKey(number)) return accountsCache.get(number);

        JsonObject jsonObject = GraphQLBankDriver.sendRequest(queryAccount(number));

        JsonElement jsonAccount = jsonObject.get("account");
        if (jsonAccount.isJsonNull()) return null;

        GraphQLAccount acc = gson.fromJson(jsonAccount, GraphQLAccount.class);
        accountsCache.put(number, acc);

        return acc;
    }

    @Override
    public String createAccount(String owner) throws IOException {
        JsonObject jsonObject = GraphQLBankDriver.sendRequest(mutationCreateAccount(owner));

        JsonElement responseElement = jsonObject.get("createAccount");
        GraphQLResponse response = gson.fromJson(responseElement, GraphQLResponse.class);

        if (response.getStatusCode() != 0) {
            return null;
        }

        GraphQLAccount acc = new GraphQLAccount();
        // XXX im Gegensatz zu getAccount wo das Konto über JSON deserialisiert wird greifen Sie hier auf die '
        //     Daten der Antwort zu. Sie könnten hier auch einfach die nummer zurückgeben, dann würde das
        //     Account-objekt beim nächsten getAccount erzeugt.
        acc.setNumber(response.getData()[0]);
        acc.setOwner(response.getData()[1]);
        acc.setBalance(Double.valueOf(response.getData()[2]));
        acc.setActive(Boolean.valueOf(response.getData()[3]));
        // XXX Wird active nicht als 0 oder 1 übertragen? Ich hätte gesagt, dass das active-Flag hier falsch gesetzt ist.
        accountsCache.put(acc.getNumber(), acc);

        return response.getData()[0];
    }

    @Override
    public boolean closeAccount(String number) throws IOException {
        JsonObject jsonObject = GraphQLBankDriver.sendRequest(mutationCloseAccount(number));
        JsonElement responseElement = jsonObject.get("closeAccount");
        GraphQLResponse response = gson.fromJson(responseElement, GraphQLResponse.class);

        if (response.getStatusCode() == 0) {
            accountsCache.get(number).setActive(false);
        }

        return response.getStatusCode() == 0;
    }

    @Override
    public void transfer(Account from, Account to, double amount) throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
        if (!(from instanceof GraphQLAccount) | !(to instanceof GraphQLAccount)) throw new IllegalArgumentException("this method is only compatible with GraphQLAccount instances");

        JsonObject jsonObject = GraphQLBankDriver.sendRequest(mutationTransfer(from.getNumber(), to.getNumber(), amount));
        JsonElement responseElement = jsonObject.get("transfer");
        GraphQLResponse response = gson.fromJson(responseElement, GraphQLResponse.class);

        if (response.getStatusCode() == SocketResponse.ERROR_INACTIVE_ACCOUNT) throw new InactiveException();
        else if (response.getStatusCode() == SocketResponse.ERROR_ACCOUNT_OVERDRAW) throw new OverdrawException();
        else if (response.getStatusCode() == SocketResponse.ERROR_ILLEGAL_ARGUMENT) throw new IllegalArgumentException();

        ((GraphQLAccount) from).setBalance(from.getBalance() - amount);
        ((GraphQLAccount) to).setBalance(to.getBalance() + amount);
    }

    private static String queryAccount(String accountNumber) {
        return String.format("query {" +
                "  account(number: \"%s\") {" +
                "    number" +
                "    owner" +
                "    balance" +
                "    active" +
                "  }" +
                "}", accountNumber);
    }

    private static String queryAccounts() {
        return "query { accounts }";
    }

    private static String mutationCreateAccount(String owner) {
        return String.format("mutation {" +
                "createAccount(owner: \"%s\") {" +
                "    statusCode" +
                "    data" +
                "  }" +
                "}", owner);
    }

    private static String mutationTransfer(String accountNumberSender, String accountNumberReceiver, double amount) {
        return String.format("mutation {" +
                "  transfer(accountNumberSender: \"%s\" accountNumberReceiver: \"%s\" amount: %s) {" +
                "    statusCode" +
                "    data" +
                "  }" +
                "}", accountNumberSender, accountNumberReceiver, amount);
    }

    private static String mutationCloseAccount(String accountNumber) {
        return String.format("mutation {" +
                "  closeAccount(accountNumber: \"%s\") {" +
                "    statusCode" +
                "    data" +
                "  }" +
                "}", accountNumber);
    }
}
