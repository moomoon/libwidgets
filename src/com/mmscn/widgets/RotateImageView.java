package com.mmscn.widgets;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.Scroller;

public class RotateImageView extends ImageView {

	private Scroller mScroller;
	private float mHeight, mWidth;
	private int mIntrinsicHeight, mIntrinsicWidth;
	private Handler mHandler = new Handler(Looper.getMainLooper());
	private float mCurrDeg = 0f;
	private boolean isInAnim = false;

	public RotateImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mScroller = new Scroller(context);
		setScaleType(ScaleType.MATRIX);
	}

	@Override
	public void setImageDrawable(Drawable drawable) {

		mIntrinsicHeight = drawable.getIntrinsicHeight();
		mIntrinsicWidth = drawable.getIntrinsicWidth();
		super.setImageDrawable(drawable);

	}

	public void rotate(final double fromDeg, final double toDeg, final long duration, long startOffset) {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				isInAnim = true;
				mScroller.abortAnimation();
				int fromDegE6 = (int) (fromDeg * 1E6);
				int toDegE6 = (int) (toDeg * 1E6);
				mScroller.startScroll(fromDegE6, 0, toDegE6 - fromDegE6, 0, (int) duration);
				invalidate();
			}
		}, startOffset);
	}

	// @Override
	// protected void onDraw(Canvas canvas) {
	// canvas.save();
	// canvas.rotate(mCurrDeg, mIntrinsicWidth / 2, mIntrinsicHeight / 2);
	// super.onDraw(canvas);
	// canvas.restore();
	// }

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			// mCurrDeg = mScroller.getCurrX() / 1E6F;
			Matrix m = new Matrix();
			// float center = Math.min(mWidth / 2, mHeight / 2);
			m.postRotate(mScroller.getCurrX() / 1E6F, getMeasuredWidth() / 2, getMeasuredHeight() / 2);
			setImageMatrix(m);
			invalidate();
		} else {
			isInAnim = false;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (!isInAnim) {
			this.mWidth = MeasureSpec.getSize(widthMeasureSpec);
			this.mHeight = MeasureSpec.getSize(heightMeasureSpec);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

}
