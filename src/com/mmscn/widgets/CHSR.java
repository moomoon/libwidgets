package com.mmscn.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class CHSR extends FrameLayout {
	private final float DEFAULT_SENSITIVITY = 1f;
	private int mHeight, mWidth;
	private int mMaxChildWidth;
	private final float mSensitivity;
	private final int mRetainWidth;
	private final Scroller mScroller;
	private final GestureDetector mDetector;
	private float mOffsetX;
	private boolean mIsEnabledOverriden = true;
	private boolean isCollapsingEnabled = true;
	private Drawable mRetainedBg;

	public CHSR(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CHSR);
		mSensitivity = a.getFloat(R.styleable.CHSR_sensitivity,
				DEFAULT_SENSITIVITY);
		mRetainWidth = a.getDimensionPixelSize(R.styleable.CHSR_retainWidth, 0);
		mRetainedBg = a.getDrawable(R.styleable.CHSR_retainBackground);
		a.recycle();
		mScroller = new Scroller(context);
		mDetector = new GestureDetector(context,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onScroll(MotionEvent e1, MotionEvent e2,
							float distanceX, float distanceY) {
						if (Math.abs(distanceY) < mSensitivity
								* Math.abs(distanceX)) {
							scrollX(distanceX);
							return true;
						}
						return false;
					}

					@Override
					public boolean onDown(MotionEvent e) {
						mScroller.abortAnimation();
						return false;
					}

					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY) {
						if (Math.abs(velocityY) < mSensitivity
								* Math.abs(velocityX)) {
							mScroller.abortAnimation();
							mScroller.fling((int) mOffsetX, 0, (int) velocityX,
									0, getMinScroll(), 0, 0, 0);
							invalidate();
						}
						return false;
					}
				});
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			mOffsetX = mScroller.getCurrX();
			invalidate();
		}
	}

	private int mLastAction = MotionEvent.ACTION_CANCEL;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (isCollapsingEnabled) {
			if (mDetector.onTouchEvent(ev)) {
				ev.setAction(MotionEvent.ACTION_CANCEL);
			}
			getTranslatedEvent(ev);
		}
		int maskedAction = ev.getAction() & MotionEvent.ACTION_MASK;
		boolean intercepted = mLastAction == MotionEvent.ACTION_CANCEL
				&& maskedAction == MotionEvent.ACTION_CANCEL;
		mLastAction = maskedAction;
		return intercepted || super.dispatchTouchEvent(ev);
	}

	private void getTranslatedEvent(MotionEvent ev) {
		final float x = ev.getX();
		if (x > mRetainWidth) {
			ev.setLocation(x - mOffsetX, ev.getY());
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (null != mRetainedBg) {
			mRetainedBg.draw(canvas);
		}
		if (isCollapsingEnabled) {
			canvas.save();
			canvas.clipRect(0, 0, mRetainWidth, mHeight);
			super.dispatchDraw(canvas);
			canvas.restore();
			canvas.save();
			canvas.clipRect(mRetainWidth, 0, mWidth, mHeight);
			canvas.translate(mOffsetX, 0);
			super.dispatchDraw(canvas);
			canvas.restore();
		} else
			super.dispatchDraw(canvas);
	}

	public void scrollToX(float x) {
		mOffsetX = Math.min(0, Math.max(getMinScroll(), x));
		invalidate();
	}

	private int getMinScroll() {
		return mWidth - mMaxChildWidth;
	}

	public void scrollX(float x) {
		scrollToX(mOffsetX - x);
	}

	public float getCollapseX() {
		return mOffsetX;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int maxChildWidth = 0;
		final int count = getChildCount();
		if (count > 0)
			for (int i = 0; i < count; i++) {
				maxChildWidth = Math.max(maxChildWidth, getChildAt(i)
						.getWidth());
			}
		Log.e("chsr", "max child width is " + maxChildWidth);
		this.mMaxChildWidth = maxChildWidth;
		isCollapsingEnabled = mIsEnabledOverriden && mMaxChildWidth > mWidth;
		scrollX(0);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		this.mHeight = MeasureSpec.getSize(heightMeasureSpec);
		this.mWidth = MeasureSpec.getSize(widthMeasureSpec);
		if (null != mRetainedBg) {
			mRetainedBg.setBounds(0, 0, mRetainWidth, mHeight);
		}
	}
}
