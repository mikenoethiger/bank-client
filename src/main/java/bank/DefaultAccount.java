package bank;

import java.util.Date;

public class DefaultAccount implements Account {

    private static final String IBAN_PREFIX = "CH56";
    private static final Object LOCK = new Object();
    private static long next_account_number = 1000_0000_0000_0000_0L;

    private String number;
    private String owner;
    private double balance;
    private boolean active = true;

    private Date lastModified;

    public DefaultAccount(String owner) {
        this.owner = owner;
        synchronized (LOCK) {
            this.number = IBAN_PREFIX + next_account_number++;
        }
        this.balance = 0;
        lastModified = new Date();
    }

    @Override
    public double getBalance() {
        return balance;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public String getNumber() {
        return number;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void deposit(double amount) throws InactiveException {
        if (!isActive()) throw new InactiveException();
        if (amount < 0) throw new IllegalArgumentException("negative amount not allowed");
        balance += amount;
        lastModified = new Date();
    }

    @Override
    public void withdraw(double amount) throws InactiveException, OverdrawException {
        if (amount < 0) throw new IllegalArgumentException("negative amount not allowed");
        if (amount > balance) throw new OverdrawException();
        if (!isActive()) throw new InactiveException();
        balance -= amount;
        lastModified = new Date();
    }

    public void setBalance(double balance) {
        this.balance = balance;
        lastModified = new Date();
    }

    void makeInactive() {
        active = false;
        lastModified = new Date();
    }

}
