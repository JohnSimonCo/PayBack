package com.johnsimon.payback.util;

import android.content.Context;

import com.johnsimon.payback.R;
import com.johnsimon.payback.currency.UserCurrency;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Payment;

import java.util.ArrayList;

public class ShareStringGenerator {
	public static String generateDebtSummary(Context context, ArrayList<Debt> debts, UserCurrency currency) {
		StringBuilder builder = new StringBuilder();

		float total = AppData.total(debts);
		if(total != 0) {
			builder.append(String.format(context.getString(R.string.debtsummary_headerformat),
					total > 0 ? context.getString(R.string.youoweme) : context.getString(R.string.ioweyou),
					currency.render(total)));
		} else {
			builder.append(context.getString(R.string.even));
		}
		builder.append("\n");

		ArrayList<Debt> currentDebts = new ArrayList<>(), partialDebts = new ArrayList<>(), paidBackDebts = new ArrayList<>();

		for(Debt debt : debts) {
			if(debt.isPaidBack()) {
				paidBackDebts.add(debt);
			} else if(debt.isPartiallyPaidBack()) {
				partialDebts.add(debt);
			} else {
				currentDebts.add(debt);
			}
		}

		generateDebtList(context, currency, currentDebts, context.getString(R.string.debtsummary_currentdebts), false, builder);
		generateDebtList(context, currency, partialDebts, context.getString(R.string.debtsummary_partialdebts), true, builder);
		generateDebtList(context, currency, paidBackDebts, context.getString(R.string.debtsummary_paidbackdebts), false, builder);

		//Trim the string: just in case
		return builder.toString().trim();
	}

	private static void generateDebtList(Context context, UserCurrency currency, ArrayList<Debt> debts, String title, boolean includeHistory, StringBuilder builder) {
		if(debts.size() <= 0) return;
		
		builder.append("\n");
		builder.append(title);
		builder.append(":\n");

		for(Debt debt : debts) {
			builder.append("\u2022 ");
			generateDebtShareString(context, debt, currency, false, builder);
			builder.append("\n");

			if(includeHistory) {
				builder.append("\t");
				builder.append(context.getString(R.string.debtsummary_history_header));
				builder.append(":\n");

				for(Payment payment : debt.getPayments()) {
					builder.append("\t");
					builder.append(String.format(context.getString(R.string.debtsummary_history_entryformat),
							currency.render(payment.amount),
							Resource.monthDateFormat.format(payment.date)));
					builder.append("\n");
				}
			}
		}
	}

	public static String generateDebtShareString(Context context, Debt debt, UserCurrency currency) {
		StringBuilder builder = new StringBuilder();
		generateDebtShareString(context, debt, currency, true, builder);
		return builder.toString();
	}

	public static void generateDebtShareString(Context context, Debt debt, UserCurrency currency, boolean fullVersion, StringBuilder builder) {
		boolean partiallyPaidBack = debt.isPartiallyPaidBack();

		builder.append(debt.amount < 0
				? debt.isPaidBack()
				? context.getString(R.string.iowedyou)
				: context.getString(R.string.ioweyou)
				: debt.isPaidBack()
				? context.getString(R.string.youowedme)
				: context.getString(R.string.youoweme));

		builder.append(" ");
		builder.append(currency.render(partiallyPaidBack ? debt.getRemainingAbsoluteDebt() : debt.getAmount()));
		builder.append(" ");

		if(partiallyPaidBack) {
			builder.append("(");
			builder.append(String.format(context.getString(R.string.debtshare_fulldebtformat),
					currency.render(debt.getAmount())));
			builder.append(") ");
		}

		if(debt.getNote() == null) {
			builder.append(context.getString(R.string.debt_incash));
		} else {
			builder.append(String.format(context.getString(R.string.debtshare_noteformat),
					debt.getNote()));
		}

		builder.append(" ");
		builder.append(String.format(context.getString(R.string.debtshare_dateformat),
				Resource.monthDateFormat.format(debt.timestamp)));

		if(debt.hasPayment() && debt.isPaidBack() || (fullVersion && partiallyPaidBack)) {
			Payment payment = debt.getLastPayment();
			builder.append(" (");
			if(partiallyPaidBack) {
				builder.append(String.format(context.getString(R.string.debtshare_partialformat),
						currency.render(debt.getPaidBackAmount()),
						Resource.monthDateFormat.format(payment.date)));

			} else {
				builder.append(String.format(context.getString(R.string.debtshare_paidbackformat),
						Resource.monthDateFormat.format(payment.date)));
			}
			builder.append(")");
		}

	}
	public static String generateDebtNotificationString(Context context, Debt debt, UserCurrency currency) {
        StringBuilder builder = new StringBuilder();

        int format = debt.getAmount() > 0 ? R.string.notif_they_owe : R.string.notif_you_owe;
        builder.append(context.getString(format, debt.getOwner().getName(), currency.render(debt.getRemainingAbsoluteDebt())));

		builder.append(" ");

		if(debt.getNote() == null) {
			builder.append(context.getString(R.string.debt_incash));
		} else {
			builder.append(String.format(context.getString(R.string.debtshare_noteformat),
					debt.getNote()));
		}

        return builder.toString();
	}
	/*
	I owe you a total of £ 84.85

	Current debts:
	• I owe you £ 100 in cash since Feb 9
	• You owe me £ 15.15 for "Milk at the grocery store" since Feb 9

	Partially paid back debts:
	• You owed me £ 1 234 for "Vodka" since Feb 10 (paid back $200 on May 19)

	Paid back debts:
	• You owed me £ 1 234 for "Vodka" since Feb 10 (paid back on May 19)
	• I owed you £ 15.5 in cash since Feb 9 (on May 19)
	• You owed me £ 15 in cash since Feb 9
	* */
}
