package com.johnsimon.payback.util;

import com.google.gson.Gson;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.serialize.SaveDataSerializable;

import java.util.ArrayList;

public class SaveData {
	public ArrayList<Person> people;
	public ArrayList<Debt> debts;

	public SaveData() {
		this.people = new ArrayList<>();
		this.debts = new ArrayList<>();
	}

    public SaveData(ArrayList<Person> people, ArrayList<Debt> debts) {
        this.people = people;
        this.debts = debts;
    }

    public static SaveData fromJson(String JSON) {
        return JSON == null ? new SaveData() : new Gson().fromJson(JSON, SaveDataSerializable.class).extract();
    }

    public static String toJson(SaveData data) {
        return new Gson().toJson(new SaveDataSerializable(data), SaveDataSerializable.class);
    }
}
