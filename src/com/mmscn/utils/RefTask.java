package com.mmscn.utils;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public abstract class RefTask<T, P, R> extends AsyncTask<Void, P, R> {
	private final Class<T> T;
	private final Handler mHander;
	private final Reference<T> ref;

	public RefTask(final T t) {
		T = (Class<T>) t.getClass();
		ref = new WeakReference<T>(t);
		mHander = new Handler(Looper.getMainLooper());
		mHander.post(new Runnable() {

			@Override
			public void run() {
				onPreExecute(t);
			}
		});
	}

	protected void post(Runnable r) {
		mHander.post(r);
	}

	protected void postDelayed(Runnable r, int delayMillis) {
		mHander.postDelayed(r, delayMillis);
	}

	protected void onPreExecute(T instance) {
	}

	@Override
	protected void onPostExecute(R result) {
		onResult(ref.get(), result);
	}

	protected void onResult(T instance, R result) {
		if (null != instance) {
			onResultWithValidInstance(instance, result);
		} else {
			Log.e("RefTask", "Reference of " + T + " is no longer valid");
		}
		onPostResult();
	};

	protected void onResultWithValidInstance(T instance, R result) {

	}

	protected void onPostResult() {

	}

	protected T peekInstance() {
		return ref.get();
	}
}
