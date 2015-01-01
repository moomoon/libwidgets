package com.mmscn.widgets;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

public class EventScrollView extends ScrollView {

	private OnScrollEventListener mListener;
	private OnScrollEventListener mViewListener;
	private int l, t;
	private final Runnable mScrollFlusher = new Runnable() {

		@Override
		public void run() {
			dispatchScroll(getScrollX(), getScrollY(), l, t);
		}
	};
	private final Handler mHandler = new Handler();

	public EventScrollView(Context context) {
		this(context, null);
	}

	public EventScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, index, params);
		if (child instanceof OnScrollEventListener) {
			mViewListener = (OnScrollEventListener) child;
		}
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		dispatchScroll(l, t, oldl, oldt);
		mHandler.removeCallbacks(mScrollFlusher);
		mHandler.post(mScrollFlusher);
	}

	protected void dispatchScroll(int l, int t, int oldl, int oldt) {
		if (null != mListener) {
			mListener.onScroll(l, t, oldl, oldt, getWidth(), getHeight());
		}
		if (null != mViewListener) {
			mViewListener.onScroll(l, t, oldl, oldt, getWidth(), getHeight());
		}
		this.l = l;
		this.t = t;
	}

	public void setOnScrollEventListener(OnScrollEventListener l) {
		this.mListener = l;
	}

	public void setViewOnScrollEventListener(OnScrollEventListener l) {
		this.mViewListener = l;
	}

	public interface OnScrollEventListener {
		public void onScroll(int l, int t, int oldl, int oldt, int width, int height);
	}
}
