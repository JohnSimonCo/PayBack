package com.johnsimon.payback.data;

import android.content.Context;
import android.widget.RelativeLayout;

import com.google.gson.annotations.SerializedName;
import com.johnsimon.payback.R;
import com.johnsimon.payback.currency.UserCurrency;
import com.johnsimon.payback.ui.MaterialPreferenceActivity;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.ShareStringGenerator;

import org.apache.http.impl.cookie.BasicMaxAgeHandler;

import java.util.ArrayList;
import java.util.UUID;

public class Debt extends SyncedData<Debt> implements Identifiable {
	private final static int POSITIVE_COLOR = R.color.green;
	private final static int NEGATIVE_COLOR = R.color.red;

	private final static int POSITIVE_COLOR_DISABLED = R.color.green_disabled;
	private final static int NEGATIVE_COLOR_DISABLED = R.color.red_disabled;

	@SerializedName("id")
	public final UUID id;

	//Used for (de)serialization
	@SerializedName("ownerId")
	public UUID ownerId;

	@SerializedName("amount")
	public double amount;

	@SerializedName("note")
	public String note;

	@SerializedName("timestamp")
	public long timestamp;

	@SerializedName("paidBack")
	public boolean paidBack;

	@SerializedName("payments")
	public ArrayList<Payment> payments;

	@SerializedName("currencyId")
	public String currencyId;

	@SerializedName("remindDate")
	public Long remindDate;

	public transient Person owner;


	public Debt(UUID ownerId, double amount, String note, UUID id, long timestamp, long touched, boolean paidBack, ArrayList<Payment> payments, String currency) {
		super(touched);

		this.id = id;
		this.ownerId = ownerId;
		this.amount = amount;
		this.note = note;
		this.timestamp = timestamp;
		this.paidBack = paidBack;
		this.payments = payments;
		this.currencyId = currency;
	}

    public Debt(Person owner, double amount, String note, UUID id, long timestamp, long touched, boolean paidBack, ArrayList<Payment> payments, String currency) {
		super(touched);

        this.id = id;
        this.owner = owner;
		this.ownerId = owner.id;
        this.amount = amount;
        this.note = note;
        this.timestamp = timestamp;
        this.paidBack = paidBack;
		this.payments = payments;
		this.currencyId = currency;
    }

	private Debt(Person owner, double amount, String note, long time, String currencyId) {
        this(owner, amount, note, UUID.randomUUID(), time, time, false, new ArrayList<Payment>(), currencyId);
	}

	//Used when creating new
	public Debt(Person owner, double amount, String note, String currencyId) {
		this(owner, amount, note, System.currentTimeMillis(), currencyId);
	}

	public void linkOwner(ArrayList<Person> people) {
		for(Person person : people) {
			if(person.id.equals(ownerId)) {
				this.owner = person;
				return;
			}
		}
	}

	public Person getOwner() {
		return owner;
	}

	public void setOwner(Person owner) {
		this.owner = owner;
		this.ownerId = owner.id;
        touch();
	}

	public double getAmount() {
		return amount;
	}


	public double getAbsoluteAmount() {
		return Math.abs(amount);
	}

	public void setAmount(double amount) {
		touch();
		this.amount = amount;
		payments.clear();
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		touch();
		this.note = note;
	}

	public boolean isPaidBack() {
		return paidBack;
	}

	public boolean isPartiallyPaidBack() {
		return !isPaidBack() && hasPayment();
	}

	public void payback() {
		setPaidBack(true);
		addPaymentWithoutCheck(getRemainingDebt());
	}

	public void unpayback() {
		setPaidBack(false);
		if(payments != null) {
			payments.clear();
		} else {
			payments = new ArrayList<>();
		}
	}

	public void setPaidBack(boolean isPaidBack) {
		touch();
		this.paidBack = isPaidBack;
	}

	//Returns whether or not the debts is paid back
	public void addPayment(double amount) {
		double remaining = getRemainingAbsoluteDebt(), payment = Math.min(amount, remaining);

		addPaymentWithoutCheck(payment);

		if(remaining - payment <= 0) {
			setPaidBack(true);
		}
	}
	public void addPaymentWithoutCheck(double amount) {
		touch();
		if(payments == null) {
			payments = new ArrayList<>();
		}
		payments.add(new Payment(amount, System.currentTimeMillis()));
	}

	public double getRemainingAbsoluteDebt() {
		return (getAbsoluteAmount() - getPaidBackAmount());
	}
	public double getRemainingDebt() {
		return getRemainingAbsoluteDebt() * Math.signum(getAmount());
	}

	public double getPaidBackAmount() {
		if(payments == null) return 0;

		double sum = 0;
		for(Payment payment : payments) {
			sum += payment.amount;
		}
		return sum;
	}

	public Long getDatePaidBack() {
		if(payments == null) return null;

		if(!isPaidBack() || payments.size() < 1) {
			return null;
		} else {
			//Return date of last transaction
			return getLastPayment().date;
		}
	}

	public void setRemindDate(Long remindDate) {
		touch();
		this.remindDate = remindDate;
	}

	public Long getRemindDate() {
		return remindDate;
	}

	public boolean hasReminder() {
		return remindDate != null;
	}

	public ArrayList<Payment> getPayments() {
		return payments;
	}

	public Payment getLastPayment() {
		return payments.get(payments.size() - 1);
	}

	public boolean hasPayment() {
		return payments != null && payments.size() > 0;
	}

	public void edit(Person owner, double amount, String note) {
        touch();
		this.owner = owner;
		this.amount = amount;
		this.note = note;
	}

	public void changeDate(long time) {
		touch();
		this.timestamp = time;
	}

	public int getColor() {
		return amount > 0 ? POSITIVE_COLOR : NEGATIVE_COLOR;
	}

	public int getDisabledColor() {
		return amount > 0 ? POSITIVE_COLOR_DISABLED : NEGATIVE_COLOR_DISABLED;
	}
	/*
	public static int getColor(float amount) {
		return amount > 0 ? POSITIVE_COLOR : NEGATIVE_COLOR;
	}

	public static int getDisabledColor(float amount) {
		return amount > 0 ? POSITIVE_COLOR_DISABLED : NEGATIVE_COLOR_DISABLED;
	}
	*/

	public static String totalString(double amount, UserCurrency userCurrency, String even, boolean isAll, String allEvenString) {
		if (amount == 0) {
			return isAll ? allEvenString : even;
		} else {
			return (amount > 0 ? "+ " : "- ") + userCurrency.render(amount);
		}
	}

	//Method to get a string usable for sharing.
	public String getShareString(Context context, UserCurrency currency) {
		return ShareStringGenerator.generateDebtShareString(context, this, currency);
	}

    @Override
    public UUID getId() {
        return id;
    }

	public int getIntegerId() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof Debt))return false;
		Debt other = (Debt) o;

		return id.equals(other.id)
			&& owner.id.equals(other.owner.id)
			&& amount == other.amount
			&& Resource.nullEquals(note, other.note)
			&& paidBack == other.paidBack
			&& Resource.nullEquals(payments, other.payments);
	}

	//TODO could implement new syncWith to sync payments histories adequately

	@Override
	public String toString() {
		return amount + " for " + note;
	}
}
