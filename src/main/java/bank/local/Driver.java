/*
 * Copyright (c) 2020 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package bank.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import bank.InactiveException;
import bank.OverdrawException;

public class Driver implements bank.BankDriver {
	private Bank bank = null;

	@Override
	public void connect(String[] args) {
		bank = new Bank();
		System.out.println("connected...");
	}

	@Override
	public void disconnect() {
		bank = null;
		System.out.println("disconnected...");
	}

	@Override
	public bank.Bank getBank() {
		return bank;
	}

	public static class Bank implements bank.Bank {

		private final Map<String, Account> accounts = new HashMap<>();

		@Override
		public Set<String> getAccountNumbers() {
			return accounts.values().stream().filter(Account::isActive).map(Account::getNumber).collect(Collectors.toSet());
		}

		@Override
		public String createAccount(String owner) {
			Account a = new Account(owner);
			accounts.put(a.number, a);
			return a.number;
		}

		@Override
		public boolean closeAccount(String number) {
			if (!accounts.containsKey(number)) return false;
			Account a = accounts.get(number);
			if (!a.isActive()) return false;
			if (a.balance > 0) return false;
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

	private static class Account implements bank.Account {
		private static final String IBAN_PREFIX = "CH56";
		private static final Object LOCK = new Object();
		private static long next_account_number = 1000_0000_0000_0000_0L;

		private String number;
		private String owner;
		private double balance;
		private boolean active = true;

		private Account(String owner) {
			this.owner = owner;
			synchronized (LOCK) {
				this.number = IBAN_PREFIX + next_account_number++;
			}
			this.balance = 0;
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
		}

		@Override
		public void withdraw(double amount) throws InactiveException, OverdrawException {
			if (amount < 0) throw new IllegalArgumentException("negative amount not allowed");
			if (amount > balance) throw new OverdrawException();
			if (!isActive()) throw new InactiveException();
			balance -= amount;
		}

		void makeInactive() {
			active = false;
		}
	}

}