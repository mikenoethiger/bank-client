/*
 * Copyright (c) 2020 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package bank.socket;

import bank.InactiveException;
import bank.OverdrawException;
import bank.ServerException;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Driver implements bank.BankDriver {

	private final String DEFAULT_REMOTE_ADDRESS = "127.0.0.1";
	private final int DEFAULT_REMOTE_PORT = 50001;

	private Bank bank = null;
	private Socket socket = null;

	@Override
	public void connect(String[] args) throws IOException {
		if (args.length > 1) {
			socket = new Socket(args[0], Integer.valueOf(args[1]));
		} else if (args.length > 0) {
			socket = new Socket(args[0], DEFAULT_REMOTE_PORT);
		} else {
			socket = new Socket(DEFAULT_REMOTE_ADDRESS, DEFAULT_REMOTE_PORT);
		}
		bank = new Bank(socket);
		System.out.println("connected...");
	}

	@Override
	public void disconnect() throws IOException {
		socket.close();
		bank = null;
		System.out.println("disconnected...");
	}

	@Override
	public bank.Bank getBank() {
		return bank;
	}

	public static class Bank implements bank.Bank {

		private final Socket socket;

		/* client side account cache to maintain consistency for multiple
		   references to the same account */
		private final Map<String, Account> accounts_cache;

		public Bank(Socket socket) {
			this.socket = socket;
			this.accounts_cache = new HashMap<>();
		}

		@Override
		public Set<String> getAccountNumbers() throws IOException {
			String[] response = Request.sendRequest(new Request.GetAccountNumbers(), socket).array();
			Set<String> output = new HashSet<>();
			for (int i = 1; i < response.length; i++) output.add(response[i]);
			return output;
		}

		@Override
		public String createAccount(String owner) throws IOException {
			Response response = Request.sendRequest(new Request.CreateAccount(owner), socket);
			if (!response.isOK()) {
				return null;
			}
			Account account = parseAccount(response.array(), socket);
			accounts_cache.put(account.getNumber(), account);
			return account.getNumber();
		}

		@Override
		public boolean closeAccount(String number) throws IOException {
			Response response = Request.sendRequest(new Request.CloseAccount(number), socket);
			if (response.isOK()) {
				accounts_cache.get(number).active = false;
			}
			return response.isOK();
		}

		@Override
		public bank.Account getAccount(String number) throws IOException {
			if (number == null || number.length() == 0) return null;
			if (accounts_cache.containsKey(number)) return accounts_cache.get(number);

			Response response = Request.sendRequest(new Request.GetAccount(number), socket);
			if (response.isOK()) {
				String[] r = response.array();
				Account a = parseAccount(r, socket);
				accounts_cache.put(number, a);
				return a;
			}
			return null;
		}

		@Override
		public synchronized void transfer(bank.Account from, bank.Account to, double amount)
				throws IOException, InactiveException, OverdrawException {
			Response response = Request.sendRequest(new Request.Transfer(from.getNumber(), to.getNumber(), amount), socket);
			if (response.isOK()) {
				double balance_from = Double.parseDouble(response.array()[1]);
				double balance_to = Double.parseDouble(response.array()[2]);
				((Account) from).setBalance(balance_from);
				((Account) to).setBalance(balance_to);
			} else if (response.getErrorCode() == Response.ERROR_INACTIVE_ACCOUNT) throw new InactiveException();
			else if (response.getErrorCode() == Response.ERROR_ACCOUNT_OVERDRAW) throw new OverdrawException();
			else if (response.getErrorCode() == Response.ERROR_ILLEGAL_ARGUMENT) throw new IllegalArgumentException();
			else throw new ServerException("" + response.getErrorCode() + " " + response.getErrorText());
		}

		private static Account parseAccount(String[] r, Socket socket) {
			return new Account(socket, r[1], r[2], Double.parseDouble(r[3]), Integer.parseInt(r[4]) != 0);
		}
	}

	public static class Account implements bank.Account {

		private final Socket socket;
		private final String number;
		private final String owner;
		private boolean active;
		private double balance;

		private Account(Socket socket, String number, String owner, double balance, boolean active) {
			this.socket = socket;
			this.number = number;
			this.owner = owner;
			this.balance = balance;
			this.active = active;
		}

		@Override
		public double getBalance() {
			return balance;
		}

		synchronized void setBalance(double balance) {
			this.balance = balance;
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
		public void deposit(double amount) throws InactiveException, IOException {
			Response response = Request.sendRequest(new Request.Deposit(getNumber(), amount), socket);
			if (response.isOK()) {
				double balance = Double.parseDouble(response.array()[1]);
				setBalance(balance);
			} else if (response.getErrorCode() == Response.ERROR_INACTIVE_ACCOUNT) throw new InactiveException();
			else if (response.getErrorCode() == Response.ERROR_ILLEGAL_ARGUMENT) throw new IllegalArgumentException();
			else throw new ServerException("" + response.getErrorCode() + " " + response.getErrorText());
		}

		@Override
		public void withdraw(double amount) throws InactiveException, OverdrawException, IOException {
			Response response = Request.sendRequest(new Request.Withdraw(getNumber(), amount), socket);
			if (response.isOK()) {
				double balance = Double.parseDouble(response.array()[1]);
				setBalance(balance);
			} else if (response.getErrorCode() == Response.ERROR_INACTIVE_ACCOUNT) throw new InactiveException();
			else if (response.getErrorCode() == Response.ERROR_ACCOUNT_OVERDRAW) throw new OverdrawException();
			else if (response.getErrorCode() == Response.ERROR_ILLEGAL_ARGUMENT) throw new IllegalArgumentException();
			else throw new ServerException("" + response.getErrorCode() + " " + response.getErrorText());
		}
	}
}