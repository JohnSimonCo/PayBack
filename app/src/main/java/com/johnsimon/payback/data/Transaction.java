package com.johnsimon.payback.data;

import java.util.UUID;

public class Transaction implements Identifiable {
	public UUID id;
	public float amount;
	public long date;

	public Transaction(float amount, Long date) {
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
		if (!(o instanceof Transaction))return false;
		Transaction other = (Transaction) o;

		return id.equals(other.id)
			&& amount == other.amount
			&& date == other.date;
	}
}
