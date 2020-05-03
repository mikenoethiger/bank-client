/*
 * Copyright (c) 2020 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package bank.local;

import bank.Bank;
import bank.protocol.InMemoryBank;

public class LocalBankDriver implements bank.BankDriver {
	private Bank bank = null;

	@Override
	public void connect(String[] args) {
		bank = new InMemoryBank();
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