package bank;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultBank implements Bank {

    private final Map<String, DefaultAccount> accounts = new HashMap<>();

    @Override
    public Set<String> getAccountNumbers() {
        return accounts.values().stream().filter(DefaultAccount::isActive).map(DefaultAccount::getNumber).collect(Collectors.toSet());
    }

    @Override
    public String createAccount(String owner) throws IOException {
        DefaultAccount a = new DefaultAccount(owner);
        accounts.put(a.getNumber(), a);
        return a.getNumber();
    }

    @Override
    public boolean closeAccount(String number) throws IOException {
        if (!accounts.containsKey(number)) return false;
        DefaultAccount a = accounts.get(number);
        if (!a.isActive()) return false;
        if (a.getBalance() > 0) return false;
        a.makeInactive();
        return true;
    }

    @Override
    public bank.Account getAccount(String number) {
        return accounts.get(number);
    }

    @Override
    public void transfer(bank.Account from, bank.Account to, double amount)
            throws IOException, InactiveException, OverdrawException {
        if (amount < 0) throw new IllegalArgumentException("negative amount not allowed");
        if (!from.isActive() || !to.isActive()) throw new InactiveException();
        if (from.getBalance() < amount) throw new OverdrawException();
        from.withdraw(amount);
        to.deposit(amount);
    }

}
