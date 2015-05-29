package com.johnsimon.payback.util;

import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.johnsimon.payback.R;
import com.johnsimon.payback.ui.base.BaseActivity;

public class Undo {
	private final static int DURATION = 3500;

	private static QueuedAction queuedAction = null;

	public static void executeAction(BaseActivity activity, int textId, final View baseView, final UndoableAction action) {
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

		Snackbar snackbar = Snackbar.make(baseView, textId, Snackbar.LENGTH_LONG).setAction(R.string.undo, new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				action.onRevert();
				handler.removeCallbacks(runnable);
				cancelAction();
			}
		});

		snackbar.show();

		action.onDisplay();
		handler.postDelayed(runnable, DURATION);

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
		void onDisplay();
		void onRevert();
		void onCommit();
	}
}
