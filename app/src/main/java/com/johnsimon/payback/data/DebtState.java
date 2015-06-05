package com.johnsimon.payback.data;

import java.util.ArrayList;
import java.util.UUID;

public class DebtState {
	public boolean paidBack;
	public ArrayList<Payment> payments;
	public Long remindDate;
	public long touched;

	public DebtState(Debt debt) {
		this.paidBack = debt.paidBack;
		this.payments = debt.payments == null ? new ArrayList<Payment>() : new ArrayList<>(debt.payments);
		this.remindDate = debt.remindDate;
		this.touched = debt.touched;
	}

	public void restore(Debt debt) {
		debt.paidBack = this.paidBack;
		debt.payments = this.payments;
		debt.remindDate = this.remindDate;
		debt.touched = this.touched;
	}
}
