package com.mmscn.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.Scroller;

public class ProportionImageView extends ImageView {
	private static final int PROP_MULTIPLIER = 100;
	private float mProportion = 1F;
	private float mStartProportion = 0F;
	private int mStartX, mStopX;
	private int mHeight, mWidth;
	private Scroller mScroller;

	public ProportionImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ProportionImageView);
		mProportion = ta.getFraction(R.styleable.ProportionImageView_displayProportion, 1, 1, 1);
		mStartProportion = ta.getFraction(R.styleable.ProportionImageView_startProportion, 1, 1, 0);
		ta.recycle();
		mScroller = new Scroller(context);
	}

	private void updateDisplayProportion() {
		this.mStartX = (int) (Math.max(0F, Math.min(1F, mStartProportion)) * mWidth);
		this.mStopX = (int) (Math.max(0F, Math.min(1F, mStartProportion + mProportion)) * mWidth);
		invalidate();
	}

	public void setDisplayProportion(float startProp, float dispProp, boolean anim) {
		if (mStartProportion != startProp || mProportion != dispProp) {
			mScroller.abortAnimation();
			if (anim) {
				int fromStart = (int) (mStartProportion * PROP_MULTIPLIER);
				int toStart = (int) (startProp * PROP_MULTIPLIER);
				int fromDisp = (int) (mProportion * PROP_MULTIPLIER);
				int toDisp = (int) (dispProp * PROP_MULTIPLIER);
				mScroller.startScroll(fromStart, fromDisp, toStart - fromStart, toDisp - fromDisp);
				invalidate();
			} else {
				mStartProportion = startProp;
				mProportion = dispProp;
				updateDisplayProportion();
			}
		}
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			mStartProportion = (float) mScroller.getCurrX() / PROP_MULTIPLIER;
			mProportion = (float) mScroller.getCurrY() / PROP_MULTIPLIER;
			updateDisplayProportion();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		canvas.clipRect(mStartX, 0F, mStopX, mHeight);
		super.onDraw(canvas);
		canvas.restore();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mWidth = MeasureSpec.getSize(widthMeasureSpec);
		mHeight = MeasureSpec.getSize(heightMeasureSpec);
		updateDisplayProportion();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
