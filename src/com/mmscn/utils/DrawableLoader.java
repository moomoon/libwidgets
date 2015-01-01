package com.mmscn.utils;

import java.lang.ref.SoftReference;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class DrawableLoader {
	private int resId;
	private SoftReference<Drawable> mSoftRef;

	public DrawableLoader(int resId) {
		this.resId = resId;
	}

	public Drawable loadDrawable(Context context) {
		Drawable d = null;
		if (null == mSoftRef || null == (d = mSoftRef.get())) {
			d = context.getResources().getDrawable(resId);
			mSoftRef = new SoftReference<Drawable>(d);
		}
		return d;
	}
}