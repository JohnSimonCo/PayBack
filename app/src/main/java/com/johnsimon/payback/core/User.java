package com.johnsimon.payback.core;

import android.content.res.Resources;
import android.text.TextUtils;

import com.johnsimon.payback.R;

/**
 * Created by John on 2014-11-13.
 */
public class User {
	public String name;
	public String[] numbers;

	public User(String name) {
		this.name = name;
	}

    public void setNumbers(String[] numbers) {
        this.numbers = numbers;
    }

	public boolean hasNumbers() {
		return numbers != null;
	}

	public String getName(Resources resources) {
		return TextUtils.isEmpty(name) ? resources.getString(R.string.you) : name;
	}
}
