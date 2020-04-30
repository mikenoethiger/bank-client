package bank;

import java.io.IOException;

/**
 * Client side Account template.
 *
 * Extend this for specific client implementations.
 * Implement {@link Account#deposit(double)} and {@link Account#withdraw(double)}
 */
public abstract class ClientAccount implements Account {

    private String number;
    private String owner;
    private boolean active;
    private double balance;

    public ClientAccount() {
    }

    public ClientAccount(String number, String owner, boolean active, double balance) {
        this.number = number;
        this.owner = owner;
        this.active = active;
        this.balance = balance;
    }

    @Override
    public String getNumber() throws IOException {
        return number;
    }

    @Override
    public String getOwner() throws IOException {
        return owner;
    }

    @Override
    public boolean isActive() throws IOException {
        return active;
    }

    @Override
    public double getBalance() throws IOException {
        return balance;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "ClientAccount{" +
                "number='" + number + '\'' +
                ", owner='" + owner + '\'' +
                ", active=" + active +
                ", balance=" + balance +
                '}';
    }
}
