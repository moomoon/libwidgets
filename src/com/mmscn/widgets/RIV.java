package com.mmscn.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RIV extends ImageView {

	private boolean isClipEnabled = true;
	private boolean isPerfetCircle = false;
	private int mRadius;
	private int mHeight, mWidth;
	private int mCenterX, mCenterY;
	private Path mClipPath;

	public RIV(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RIV);
		isPerfetCircle = ta.getBoolean(R.styleable.RIV_isPerfectCircle, false);
		isClipEnabled = ta.getBoolean(R.styleable.RIV_isClipEnabled, true);
		ta.recycle();
	}

	private void updateClipPath() {
		Path p = new Path();
		if (isPerfetCircle) {
			p.addOval(new RectF(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius, mCenterY + mRadius),
					Direction.CW);
		} else {
			p.addOval(new RectF(getPaddingLeft(), getPaddingTop(), mWidth - getPaddingRight(), mHeight
					- getPaddingBottom()), Direction.CW);
		}
		mClipPath = p;
	}

	public void setIsPerfectCircle(boolean isPerfectCircle) {
		this.isPerfetCircle = isPerfectCircle;
		updateClipPath();
		invalidate();
	}

	public void setClipEnabled(boolean enabled) {
		this.isClipEnabled = enabled;
		invalidate();
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			Drawable d = new RoundedDrawable(((BitmapDrawable) drawable).getBitmap()).setOval(true).setScaleType(
					ScaleType.CENTER_CROP);
			super.setImageDrawable(d);
			isClipEnabled = false;
			return;
		}
		isClipEnabled = true;
		super.setImageDrawable(drawable);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (isClipEnabled) {
			canvas.save();
			canvas.clipPath(mClipPath);
			super.onDraw(canvas);
			canvas.restore();
		} else {
			super.onDraw(canvas);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		mRadius = Math.min(width - getPaddingLeft() - getPaddingRight(), height - getPaddingTop() - getPaddingBottom()) / 2;
		mCenterX = width / 2;
		mCenterY = height / 2;
		mWidth = width;
		mHeight = height;
		updateClipPath();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

}
