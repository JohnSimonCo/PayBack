package com.johnsimon.payback.util;

import android.os.Handler;

import com.johnsimon.payback.R;
import com.johnsimon.payback.ui.base.BaseActivity;
import com.williammora.snackbar.Snackbar;

public class Undo {
	private final static int DURATION = 2500;
	private final static int DELAY = 600;

	private static QueuedAction queuedAction = null;

	public static void executeAction(BaseActivity activity, int textId, final UndoableAction action) {
        completeAction();
		queuedAction = new QueuedAction(action);

		final Handler handler = new Handler();
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				action.onCommit();
                cancelAction();
			}
		};

		queuedAction.handler = handler;
		queuedAction.runnable = runnable;

		Snackbar snackbar = Snackbar.with(activity.getApplicationContext());
		snackbar.text(activity.getString(textId))
			.actionLabel(activity.getString(R.string.undo))
			.actionColor(activity.getResources().getColor(R.color.undo_color))
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
                    cancelAction();
				}
			})
			.show(activity);

		queuedAction.snackbar = snackbar;
        activity.queuedActions.add(queuedAction);
	}

    private static void cancelAction() {
        queuedAction = null;
    }

	public static void completeAction() {
        if(queuedAction != null) {
            queuedAction.handler.removeCallbacks(queuedAction.runnable);
            queuedAction.action.onCommit();
            queuedAction.snackbar.dismiss();

            queuedAction = null;
        }
	}

	public static class QueuedAction {
		public UndoableAction action;
		public Snackbar snackbar;
		public Handler handler;
		public Runnable runnable;

		public QueuedAction(UndoableAction action) {
			this.action = action;
		}
	}

	public interface UndoableAction {
		public void onDisplay();
		public void onRevert();
		public void onCommit();
	}
}
