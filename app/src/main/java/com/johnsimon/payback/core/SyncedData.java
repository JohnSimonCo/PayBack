package com.johnsimon.payback.core;

public abstract class SyncedData<T extends SyncedData> {
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
