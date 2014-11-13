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

	public NfcData(ArrayList<Debt> debts) {
		this.debts = new DebtSendable[debts.size()];
		for(int i = 0; i < this.debts.length; i++) {
			this.debts[i] = new DebtSendable(debts.get(i));
		}
		this.sender = Resource.user;
	}
}