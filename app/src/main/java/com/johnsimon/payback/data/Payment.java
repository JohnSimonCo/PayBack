package com.johnsimon.payback.data;

import java.util.UUID;

public class Payment implements Identifiable {
	public UUID id;
	public double amount;
	public long date;

	public Payment(double amount, Long date) {
		this.id = UUID.randomUUID();
		this.amount = amount;
		this.date = date;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof Payment))return false;
		Payment other = (Payment) o;

		return id.equals(other.id)
			&& amount == other.amount
			&& date == other.date;
	}
}
