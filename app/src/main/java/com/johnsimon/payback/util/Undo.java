package com.johnsimon.payback.util;

import android.app.Activity;
import android.os.Handler;

import com.johnsimon.payback.R;
import com.johnsimon.payback.async.PoorMansPromise;
import com.williammora.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by John on 2015-01-21.
 */
public class Undo {
	private final static int DURATION = 2500;
	private final static int DELAY = 550;

	private static ArrayList<QueuedAction> queuedActions = new ArrayList<>();

	public static void executeAction(Activity context, int textId, final UndoableAction action) {

		final PoorMansPromise promise = new PoorMansPromise();
		final QueuedAction queuedAction = new QueuedAction(action, promise);
		final Handler handler = new Handler();
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				action.onCommit();
				queuedActions.remove(queuedAction);
			}
		};
		queuedAction.handler = handler;
		queuedAction.runnable = runnable;

		Snackbar snackbar = Snackbar.with(context.getApplicationContext());
		snackbar.text(context.getString(textId))
			.actionLabel(context.getString(R.string.undo))
			.actionColor(context.getResources().getColor(R.color.green))
			.duration(DURATION)
			.eventListener(new Snackbar.EventListener() {
				@Override
				public void onShow(int i) {
					action.onDisplay();
					handler.postDelayed(runnable, DURATION + DELAY);
				}

				@Override
				public void onDismiss(int i) {
				}
			})
			.actionListener(new Snackbar.ActionClickListener() {
				@Override
				public void onActionClicked() {
					action.onRevert();
					handler.removeCallbacks(runnable);
					queuedActions.remove(queuedAction);
				}
			})
			.show(context);

		queuedAction.snackbar = snackbar;

		queuedActions.add(queuedAction);
	}

	public static void completeActions() {
		if(queuedActions.size() > 0) {
			for(QueuedAction queuedAction : queuedActions) {
				queuedAction.handler.removeCallbacks(queuedAction.runnable);
				queuedAction.action.onCommit();
				queuedAction.snackbar.dismiss();
			}
			queuedActions.clear();
		}
	}


	private static class QueuedAction {
		public UndoableAction action;
		public PoorMansPromise promise;
		public Snackbar snackbar;
		public Handler handler;
		public Runnable runnable;

		public QueuedAction(UndoableAction action, PoorMansPromise promise) {
			this.action = action;
			this.promise = promise;
		}
	}

	public interface UndoableAction {
		public void onDisplay();
		public void onRevert();
		public void onCommit();
	}
}
