package com.mmscn.widgets;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class GraphGestureDetector {

	OnGestureListener mListener;

	private ScaleGestureDetector mScaleDetector;
	private GestureDetector mGestureDetector;
	private boolean inScale;

	public GraphGestureDetector(Context context, OnGestureListener listener) {
		inScale = false;
		mScaleDetector = new ScaleGestureDetector(context,
				new ScaleGestureDetector.SimpleOnScaleGestureListener() {
					@Override
					public boolean onScale(ScaleGestureDetector detector) {
						if (!inScale) {
							inScale = true;
							mListener.onScaleStart(detector.getFocusX());
						}
						mListener.onScale(detector.getScaleFactor());
						return true;
					}
				});

		mGestureDetector = new GestureDetector(context,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onSingleTapUp(MotionEvent ev) {
						mListener.onSingleTapUp(ev.getX(), ev.getY());
						return false;
					}

					@Override
					public boolean onScroll(MotionEvent e1, MotionEvent e2,
							float distanceX, float distanceY) {
						mListener.onScroll(distanceX);
						return true;
					}
				});
		mListener = listener;
	}

	public boolean onTouchEvent(MotionEvent ev) {
		mScaleDetector.onTouchEvent(ev);
		mGestureDetector.onTouchEvent(ev);
		switch(ev.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (inScale) {
				inScale = false;
			}
		}
		return true;
	}

	public interface OnGestureListener {
		public void onScroll(float dx);
		public void onScale(float scaleFactorX);
		public void onScaleStart(float x);
		public void onSingleTapUp(float x, float y);
	}

}