package com.johnsimon.payback.serialize;

import java.util.UUID;

public class SyncedDataSerializable {
	public UUID id;
	public long touched;

	public SyncedDataSerializable(UUID id, long touched) {
		this.id = id;
		this.touched = touched;
	}
}
