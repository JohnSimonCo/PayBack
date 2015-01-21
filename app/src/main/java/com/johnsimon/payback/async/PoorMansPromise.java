package com.johnsimon.payback.async;

public class PoorMansPromise {
	boolean fired = false;

	public boolean fire() {
		boolean shouldFire = !fired;

		fired = true;

		return shouldFire;
	}
}
