package com.mmscn.widgets;

import java.util.Map;
import java.util.WeakHashMap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class StackLayout extends RelativeLayout {

	public static final int MAX_STEP = 100000;
	public static final int MIN_STEP = 0;

	private int mStep = MIN_STEP;
	private int mFlingStep = (int) (MIN_STEP + (MAX_STEP - MIN_STEP) * 0.5);
	private int mDragSlop = 10;
	private float mSensitivity = 0.5F;

	private StackThreshold mThreshold;
	private final Map<View, StackInfo> mStackViews = new WeakHashMap<View, StackInfo>();
	private final Map<View, int[]> mOriginalPos = new WeakHashMap<View, int[]>();
	private OnStackListener mStackListener;
	private Scroller mScroller;

	private boolean mStacking = false;

	private GestureDetector mDetector;
	private int mHeight, mWidth;
	private boolean mClampTop = false;
	private boolean mClapBottom = true;

	public StackLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mScroller = new Scroller(context);
		mDetector = new GestureDetector(context,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onScroll(MotionEvent e1, MotionEvent e2,
							float distanceX, float distanceY) {
						if (mStacking) {
							doStack(distanceY);
							return true;
						}
						float totalY = e2.getY() - e1.getY();
						float ratio = Math.abs(totalY / (e2.getX() - e1.getX()));
						boolean stacking = false;
						if (ratio >= mSensitivity) {
							if (null == mThreshold
									|| mThreshold.isStackEnabled(totalY > 0,
											mStep)) {
								stacking = Math.abs(totalY) > mDragSlop;
							}
						}
						mStacking = stacking;
						return mStacking;
					}
				});
	}

	private void doStack(float distanceY) {
		int step = (int) (distanceY / mHeight * (MAX_STEP - MIN_STEP));
		mStep -= step;
		if (mClampTop) {
			mStep = Math.min(mStep, MAX_STEP);
		}
		if (mClapBottom) {
			mStep = Math.max(mStep, MIN_STEP);
		}
		offsetViews();
		invalidate();
	}

	public int getStep() {
		return mStep;
	}

	public void setStackThreshold(StackThreshold st) {
		this.mThreshold = st;
	}

	public void setOnStackListener(OnStackListener l) {
		this.mStackListener = l;
	}

	public void setClampTop(boolean clampTop) {
		this.mClampTop = clampTop;
	}

	public void setClampBottom(boolean clampBottom) {
		this.mClapBottom = clampBottom;
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			mStep = mScroller.getCurrX();
			offsetViews();
			invalidate();
		}
	}

	public void flingTo(int step) {
		mScroller.abortAnimation();
		mScroller.startScroll(mStep, 0, step - mStep, 0, 500);
		invalidate();
	}

	private void doFling() {
		if (mStep >= mFlingStep) {
			flingTo(MAX_STEP);
		} else {
			flingTo(MIN_STEP);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean stacking = mDetector.onTouchEvent(ev);
		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		switch (action) {
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (mStacking) {
				mStacking = false;
				doFling();
			}
			break;
		default:
			break;
		}

		boolean dispatched = stacking || super.dispatchTouchEvent(ev);
		return MotionEvent.ACTION_DOWN == action || dispatched;
	}

	/**
	 * 
	 * the current position of v will be treated as mStep
	 * 
	 * @param v
	 * @param maxMoveFraction
	 */
	public void addRelativeStackView(View v, float maxMoveFraction) {
		StackInfo si = new StackInfo();
		si.maxOffsetRatio = maxMoveFraction;
		si.currStep = mStep;
		mStackViews.put(v, si);
	}

	public void addStackView(View v, int maxOffset) {
		StackInfo si = new StackInfo();
		si.maxOffset = maxOffset;
		si.currStep = mStep;
		mStackViews.put(v, si);
	}

	private void offsetViews() {
		for (View v : mStackViews.keySet()) {
			offsetStackView(v);
		}

		if (null != mStackListener) {
			mStackListener.onStack(mStep);
		}
	}

	private void offsetStackView(View v) {
		synchronized (v) {
			int[] origPos = mOriginalPos.get(v);
			StackInfo si = mStackViews.get(v);
			int offset;
			try {
				int top = (int) (origPos[1] + si.getMaxOffset(v) * mStep
						/ (MAX_STEP - MIN_STEP));
				offset = top - v.getTop();
			} catch (NullPointerException e) {
				offset = (int) ((mStep - si.currStep) * si.getMaxOffset(v) / (MAX_STEP - MIN_STEP));
			}
			v.offsetTopAndBottom(offset);
			si.currStep = mStep;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mWidth = MeasureSpec.getSize(widthMeasureSpec);
		mHeight = MeasureSpec.getSize(heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		for (View v : mStackViews.keySet()) {
			mOriginalPos.put(
					v,
					new int[] { v.getLeft(), v.getTop(), v.getRight(),
							v.getBottom() });
		}
	}

	public interface OnStackListener {
		public void onStack(int step);
	}

	public interface StackThreshold {
		public boolean isStackEnabled(boolean isDownward, int step);
	}

	private static class StackInfo {
		private float maxOffsetRatio = Float.NaN;
		private int maxOffset = 0;
		private int currStep;

		private int getMaxOffset(View v) {
			if (Float.isNaN(maxOffsetRatio)) {
				return maxOffset;
			}
			return (int) (v.getMeasuredHeight() * maxOffsetRatio);
		}
	}

}
