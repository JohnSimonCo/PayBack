package com.johnsimon.payback.async;

/**
 * Created by John on 2015-01-31.
 */
public class NullPromise extends Promise<Void> {
	public void fire() {
		fire(null);
	}
}
