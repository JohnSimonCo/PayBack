package com.johnsimon.payback.send;

import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.User;

public class NfcData {
	public DebtSendable[] debts;
	public User sender;
	public boolean fullSync;

	public NfcData(Debt[] debts, User sender, boolean fullSync) {
		this.debts = new DebtSendable[debts.length];
		for(int i = 0, l = this.debts.length; i < l; i++) {
			this.debts[i] = new DebtSendable(debts[i]);
		}
		this.sender = sender;
		this.fullSync = fullSync;
	}
}