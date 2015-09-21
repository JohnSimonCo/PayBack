package com.johnsimon.payback.async;

import android.content.Context;
import android.os.AsyncTask;

public class Background {

	public static <T> Promise<T> run(Context context, BackgroundBlock<T> backgroundBlock) {
		BackgroundAsyncTask<T> asyncTask = new BackgroundAsyncTask<T>(backgroundBlock);
		asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		return asyncTask.promise;
	}

	public static class BackgroundAsyncTask<T> extends AsyncTask<Void, Void, T> {

		public BackgroundBlock<T> backgroundBlock;
		public Promise<T> promise = new Promise<>();

		public BackgroundAsyncTask(BackgroundBlock<T> backgroundBlock) {
			this.backgroundBlock = backgroundBlock;
		}

		@Override
		protected T doInBackground(Void... params) {
			return backgroundBlock.run();
		}

		@Override
		protected void onPostExecute(T result) {
			promise.fire(result);
		}
	}

}
