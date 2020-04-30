/*
 * Copyright (c) 2020 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package bank.local;

import bank.Bank;
import bank.ServerBank;

public class Driver implements bank.BankDriver {
	private Bank bank = null;

	@Override
	public void connect(String[] args) {
		bank = new ServerBank();
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

}