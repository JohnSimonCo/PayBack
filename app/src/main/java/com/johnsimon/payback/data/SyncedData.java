package com.johnsimon.payback.data;

import com.google.gson.annotations.SerializedName;

public abstract class SyncedData<T extends SyncedData> {
	@SerializedName("touched")
	public long touched;

	public SyncedData(long touched) {
		this.touched = touched;
	}

	protected void touch() {
		touched = System.currentTimeMillis();
	}

	public T syncWith(T other) {
		return this.touched > other.touched ? (T) this : other;
	}

	@Override
	public abstract boolean equals(Object o);
}
