package com.mmscn.utils;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public abstract class ViewController {
	private Reference<View> mViewRef;

	protected View createView(Context context, ViewGroup parent) {
		View v = onCreateView(context, parent);
		mViewRef = new WeakReference<View>(v);
		return v;
	}

	protected abstract View onCreateView(Context context, ViewGroup parent);

	public View getView(Context context, ViewGroup parent) {
		try {
			return mViewRef.get();
		} catch (NullPointerException e) {
			e.printStackTrace();
			return createView(context, parent);
		}
	}

	protected View peekView() {
		return mViewRef.get();
	}
}
