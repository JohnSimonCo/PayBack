package com.johnsimon.payback.core;

import com.johnsimon.payback.data.Debt;

public class UserCurrency {
	public final String id;
	public final boolean before;
	public final String displayName;

	public UserCurrency(String id, boolean before, String displayName) {
		this.id = id;
		this.before = before;
		this.displayName = displayName;
	}

	public String render(float amount) {
		return render(Float.toString(Math.abs(amount)).replaceAll("\\.0$", ""));
	}

	public String render(String amount) {
		return renderCurrency(amount, id);
	}

	public String render(Debt debt) {
		return render(debt.getAmount(), debt.currencyId);
	}

	public String render(float amount, String currencyId) {
		return render(Float.toString(Math.abs(amount)).replaceAll("\\.0$", ""), currencyId);
	}

	public String render(String amount, String currencyId) {
		return renderCurrency(amount, currencyId.equals(id) ? displayName : currencyId);
	}

	private String renderCurrency(String amount, String symbol) {
		return before ? symbol + " " + amount : amount + " " + symbol;
	}
}
