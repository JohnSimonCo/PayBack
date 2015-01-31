package com.johnsimon.payback.async;

/**
 * Created by John on 2015-01-31.
 */
public abstract class NullCallback implements Callback<Void> {
	@Override
	public void onCalled(Void data) {
		onCalled();
	}

	public abstract void onCalled();
}
