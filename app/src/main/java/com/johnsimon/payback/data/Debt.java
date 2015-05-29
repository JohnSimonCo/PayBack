package com.johnsimon.payback.data;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.johnsimon.payback.R;
import com.johnsimon.payback.currency.UserCurrency;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.ShareStringGenerator;

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
	public float amount;

	@SerializedName("note")
	public String note;

	@SerializedName("timestamp")
	public long timestamp;

	@SerializedName("paidBack")
	public boolean paidBack;

	@SerializedName("transactions")
	public ArrayList<Transaction> transactions;

	@SerializedName("currencyId")
	public String currencyId;

	@SerializedName("remindDate")
	public Long remindDate;

	public transient Person owner;


	public Debt(UUID ownerId, float amount, String note, UUID id, long timestamp, long touched, boolean paidBack, ArrayList<Transaction> transactions, String currency) {
		super(touched);

		this.id = id;
		this.ownerId = ownerId;
		this.amount = amount;
		this.note = note;
		this.timestamp = timestamp;
		this.paidBack = paidBack;
		this.transactions = transactions;
		this.currencyId = currency;
	}

    public Debt(Person owner, float amount, String note, UUID id, long timestamp, long touched, boolean paidBack, ArrayList<Transaction> transactions, String currency) {
		super(touched);

        this.id = id;
        this.owner = owner;
		this.ownerId = owner.id;
        this.amount = amount;
        this.note = note;
        this.timestamp = timestamp;
        this.paidBack = paidBack;
		this.transactions = transactions;
		this.currencyId = currency;
    }

	private Debt(Person owner, float amount, String note, long time, String currencyId) {
        this(owner, amount, note, UUID.randomUUID(), time, time, false, new ArrayList<Transaction>(), currencyId);
	}

	//Used when creating new
	public Debt(Person owner, float amount, String note, String currencyId) {
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

	public float getAmount() {
		return amount;
	}

	public float getAbsoluteAmount() {
		return Math.abs(amount);
	}

	public void setAmount(float amount) {
		touch();
		this.amount = amount;
		transactions.clear();
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

	public boolean isPartlyPaidBack() {
		return !isPaidBack() && transactions != null && transactions.size() > 0;
	}

	public void payback() {
		setPaidBack(true);
		addTransactionWithoutCheck(getRemainingDebt());
	}

	public void unpayback() {
		setPaidBack(false);
		transactions.clear();
	}

	public void setPaidBack(boolean isPaidBack) {
		touch();
		this.paidBack = isPaidBack;
	}

	public void addTransaction(float amount) {
		addTransactionWithoutCheck(amount);

		if(!isPaidBack() && getPaidBackAmount() >= getAmount()) {
			setPaidBack(true);
		}
	}
	public void addTransactionWithoutCheck(float amount) {
		touch();
		if(transactions == null) {
			transactions = new ArrayList<>();
		}
		transactions.add(new Transaction(amount, System.currentTimeMillis()));
	}

	public float getRemainingDebt() {
		return (getAbsoluteAmount() - getPaidBackAmount()) * Math.signum(getAmount());
	}

	public float getPaidBackAmount() {
		if(transactions == null) return 0;

		float sum = 0;
		for(Transaction transaction : transactions) {
			sum += transaction.amount;
		}
		return sum;
	}

	public Long getDatePaidBack() {
		if(transactions == null) return null;

		if(!isPaidBack() || transactions.size() < 1) {
			return null;
		} else {
			//Return date of last transaction
			return transactions.get(transactions.size() - 1).date;
		}
	}

	public void setRemindDate(Long remindDate) {
		touch();
		this.remindDate = remindDate;
	}

	public Long getRemindDate() {
		return remindDate;
	}

	public void edit(Person owner, float amount, String note) {
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

	public static String totalString(float amount, UserCurrency userCurrency, String even, boolean isAll, String allEvenString) {
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

	public Debt copy() {
		return new Debt(owner, amount, note, id, timestamp, touched, paidBack, new ArrayList<>(transactions), currencyId);
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
			&& Resource.nullEquals(transactions, other.transactions);
	}

	//TODO could implement new syncWith to sync transaction histories adequately

	@Override
	public String toString() {
		return amount + " for " + note;
	}
}
