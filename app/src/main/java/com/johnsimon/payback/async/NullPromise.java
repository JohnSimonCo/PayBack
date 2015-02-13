package com.johnsimon.payback.async;

public class NullPromise extends Promise<Void> {
	public void fire() {
		fire(null);
	}
}
