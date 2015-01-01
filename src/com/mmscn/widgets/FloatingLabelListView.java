package com.mmscn.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class FloatingLabelListView extends ListView {
	private FloatingLabelDrawable mDrawable;
	private int mWidth, mHeight;

	public FloatingLabelListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setFloatingLabelDrawable(FloatingLabelDrawable d) {
		this.mDrawable = d;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (isFloatingLabelEnabled())
			mDrawable.draw(canvas, mWidth, mHeight);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return isFloatingLabelEnabled() || super.dispatchTouchEvent(ev);
	}

	private boolean isFloatingLabelEnabled() {
		return null != mDrawable && mDrawable.enabled();
	}

	public interface FloatingLabelDrawable {
		public boolean enabled();

		public void draw(Canvas canvas, int width, int height);
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		this.mWidth = MeasureSpec.getSize(widthMeasureSpec);
		this.mHeight = MeasureSpec.getSize(heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	};

}
