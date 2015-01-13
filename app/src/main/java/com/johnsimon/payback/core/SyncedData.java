package com.johnsimon.payback.core;

import java.util.UUID;

public abstract class SyncedData<T extends SyncedData> {
	public final UUID id;
	public long touched;

	public SyncedData(UUID id, long touched) {
		this.id = id;

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
