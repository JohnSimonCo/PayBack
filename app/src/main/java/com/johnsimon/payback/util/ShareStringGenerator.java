package com.johnsimon.payback.util;

import android.content.Context;

import com.johnsimon.payback.R;
import com.johnsimon.payback.currency.UserCurrency;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.data.Debt;

import java.util.ArrayList;

public class ShareStringGenerator {
	public static String generateDebtSummary(Context context, ArrayList<Debt> debts, UserCurrency currency) {
		StringBuilder builder = new StringBuilder();

		float total = AppData.total(debts);
		if(total != 0) {
			builder.append(total > 0 ? context.getString(R.string.youoweme) : context.getString(R.string.ioweyou));
			builder.append(" ");
			builder.append(context.getString(R.string.a_total_of));
			builder.append(" ");
			builder.append(currency.render(total));
		} else {
			builder.append(context.getString(R.string.even));
		}

		ArrayList<Debt> currentDebts = new ArrayList<>(), paidBackDebts = new ArrayList<>();
		for(Debt debt : debts) {
			if(debt.isPaidBack()) {
				paidBackDebts.add(debt);
			} else {
				currentDebts.add(debt);
			}
		}
		builder.append("\n");

		if(currentDebts.size() > 0) {
			builder.append("\n");
			builder.append("Current debts");
			builder.append(":\n");

			for(Debt debt : currentDebts) {
				builder.append("\u2022 ");
				generateDebtShareString(context, debt, currency, builder);
				builder.append("\n");
			}
		}

		if(paidBackDebts.size() > 0) {
			builder.append("\n");
			builder.append("Paid back debts");
			builder.append(":\n");

			for(Debt debt : paidBackDebts) {
				builder.append("\u2022 ");
				generateDebtShareString(context, debt, currency, builder);
				builder.append("\n");
			}
		}

		return builder.toString().trim();
	}
	public static String generateDebtShareString(Context context, Debt debt, UserCurrency currency) {
		StringBuilder builder = new StringBuilder();
		generateDebtShareString(context, debt, currency, builder);
		return builder.toString();
	}

	public static void generateDebtShareString(Context context, Debt debt, UserCurrency currency, StringBuilder builder) {
		builder.append(debt.amount < 0
				? debt.isPaidBack()
					? context.getString(R.string.iowedyou)
					: context.getString(R.string.ioweyou)
				: debt.isPaidBack()
					? context.getString(R.string.youowedme)
					: context.getString(R.string.youoweme));

		builder.append(" ");
		builder.append(currency.render(debt));
		builder.append(" ");

		if(debt.getNote() == null) {
			builder.append(context.getString(R.string.debt_incash));
		} else {
			builder.append(context.getString(R.string.debt_for));
			builder.append(" \"");
			builder.append(debt.getNote());
			builder.append("\"");
		}

		builder.append(" ");
		builder.append("since");
		builder.append(" ");
		builder.append(Resource.monthDateFormat.format(debt.timestamp));

		Long datePaidBack = debt.getDatePaidBack();
		if(datePaidBack != null) {
			builder.append(" (");
			builder.append("paid back");
			builder.append(" ");
			builder.append(Resource.monthDateFormat.format(datePaidBack));
			builder.append(")");
		}

	}
	/*
	I owe you a total of 200kr			(You owe me)

	Current debts:
	I owe you 200kr for Vodka since 16 jan
	You owe me 100kr in cash

	Paid back debts:
	I owed you 250kr for Glass since 20 mar (paid back 20 jan)
	You owed me 300kr for Bajs
	* */
}
