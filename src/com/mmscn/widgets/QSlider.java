package com.mmscn.widgets;

import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class QSlider extends View {
	private final static float DEFAULT_TEXT_SIZE = 10F;
	private final static int DEFAULT_TEXT_COLOR = Color.BLACK;
	private Paint mPt;
	private List<String> mLabels;
	private OnSlideListener mListener;
	private String mSelection = null;

	public QSlider(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.QSlider);
		float textSize = ta.getDimension(R.styleable.QSlider_android_textSize, DEFAULT_TEXT_SIZE);
		int textColor = ta.getColor(R.styleable.QSlider_android_textColor, DEFAULT_TEXT_COLOR);
		ta.recycle();
		mPt = new Paint();
		mPt.setTextSize(textSize);
		mPt.setColor(textColor);
		mPt.setAntiAlias(true);
	}

	public void setLabels(List<String> labels) {
		this.mLabels = labels;
		this.mSelection = null;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (null != mLabels) {
			final float labelItemHeight = getLabelItemHeight();
			final int left = getPaddingLeft();
			float bottom = labelItemHeight;
			for (String label : mLabels) {
				canvas.drawText(label, left, bottom, mPt);
				bottom += labelItemHeight;
			}
		}
	}

	private float getLabelItemHeight() {
		return mPt.descent() - mPt.ascent();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		String sel = null;
		int index = -1;
		if (null != mLabels && mLabels.size() > 0) {
			final float labelItemHeight = getLabelItemHeight();
			index = (int) (event.getY() / labelItemHeight);
			sel = mLabels.get(Math.max(0, Math.min(mLabels.size() - 1, index)));
		}
		if (null != sel && !sel.equals(mSelection)) {
			mSelection = sel;
			if (null != mListener) {
				mListener.onSlide(index, mSelection);
			}
		} else {
			mSelection = null;
		}
		return true;
	}

	public void setOnSlideLisetener(OnSlideListener l) {
		this.mListener = l;
	}

	public interface OnSlideListener {
		public void onSlide(int index, String label);
	}
}
