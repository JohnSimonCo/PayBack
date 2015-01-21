package com.johnsimon.payback.core;

import com.johnsimon.payback.data.Debt;

public class UserCurrency {
    //TODO tvinga currency och "mini-migrata" alla nuvarande debts till nya currencyn

	public final String id;
	public final String displayName;
	public final boolean before;

	public UserCurrency(String id, String displayName, boolean before) {
		this.id = id;
		this.displayName = displayName;
		this.before = before;
	}

	public String getDisplayName() {
		return displayName == null ? id : displayName;
	}

	public String render(float amount) {
		return render(Float.toString(Math.abs(amount)).replaceAll("\\.0$", ""));
	}

	public String render(String amount) {
		return renderCurrency(amount, getDisplayName());
	}

	public String render(Debt debt) {
		return render(debt.getAmount(), debt.currencyId);
	}

	public String render(float amount, String currencyId) {
		return render(Float.toString(Math.abs(amount)).replaceAll("\\.0$", ""), currencyId);
	}

	public String render(String amount, String currencyId) {
        //TODO efter launch: displaya rätt currency
        //return renderCurrency(amount, currencyId.equals(id) && displayName != null ? displayName : currencyId);
        return renderCurrency(amount, getDisplayName());
	}

	private String renderCurrency(String amount, String symbol) {
		return before ? symbol + " " + amount : amount + " " + symbol;
	}
}
