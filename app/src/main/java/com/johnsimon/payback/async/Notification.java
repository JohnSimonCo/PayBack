package com.johnsimon.payback.async;

import java.util.ArrayList;

public class Notification {
	private ArrayList<NotificationCallback> callbacks = new ArrayList<>();
	private boolean notify;
	private boolean notified = false;

	public Notification(boolean notify) {
		this.notify = notify;
	}

	public Notification() {
		this(true);
	}


	public void listen(NotificationCallback callback) {
		callbacks.add(callback);
		if(notified && notify) {
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

	public void clearCallbacks() {
		callbacks.clear();
	}
}
