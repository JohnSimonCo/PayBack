package com.johnsimon.payback.async;

import java.util.ArrayList;

public class Notification {
	private ArrayList<NotificationCallback> callbacks = new ArrayList<>();
	boolean notified = false;

	public void listen(NotificationCallback callback) {
		callbacks.add(callback);
		if(notified) {
			callback.onNotify();
		}
	}

	public void unregister(NotificationCallback callback) {
		callbacks.remove(callback);
	}

	public void broadcast() {
		notified = true;

		for(NotificationCallback callback : callbacks) {
			callback.onNotify();
		}
	}
}
