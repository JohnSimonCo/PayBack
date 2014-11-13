package com.johnsimon.payback.send;

import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.User;
import com.johnsimon.payback.util.Resource;

import java.util.ArrayList;

/**
 * Created by John on 2014-11-13.
 */
public class NfcData {
	public DebtSendable[] debts;
	public User sender;
	public boolean fullSync;

	public NfcData(Debt[] debts, boolean fullSync) {
		this.debts = new DebtSendable[debts.length];
		for(int i = 0; i < this.debts.length; i++) {
			this.debts[i] = new DebtSendable(debts[i]);
		}
		this.sender = Resource.user;
		this.fullSync = fullSync;
	}
}