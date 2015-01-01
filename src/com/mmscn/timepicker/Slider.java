package com.mmscn.timepicker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.mmscn.widgets.R;

public class Slider extends View {

	private int mHeight, mWidth;
	private int mLineLengthLong, mLineLengthShort;
	private int mColorBg, mColorBgSelected, mColorBgUnavailable, mColorBgPressed, mColorLine, mColorText;
	private float mTextSizeLarge, mTextSizeSmall;
	private int mTextPaddingRight, mTextPaddingBottom;
	private int mLastPos, mCurrentPos;
	private int mTotalStep = 60, mMinStep, mMaxStep = 59;
	private float mStepWidth;
	private TextView mBoundTv;

	private GestureDetector mDetector;
	private OnSliderChangedListener mListener;
	private boolean isPressed;
	private String mFormat;

	private Paint mPt;

	public Slider(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		Resources res = context.getResources();

		mLineLengthShort = (int) res.getDimension(R.dimen.time_picker_line_short);
		mLineLengthLong = (int) res.getDimension(R.dimen.time_picker_line_long);

		mColorBg = res.getColor(R.color.time_picker_stripe);
		mColorBgUnavailable = res.getColor(R.color.time_picker_stripe_unavailable);
		mColorBgPressed = res.getColor(R.color.time_picker_stripe_pressed);
		mColorBgSelected = res.getColor(R.color.time_picker_stripe_selected);
		mColorLine = res.getColor(R.color.time_picker_line);
		mColorText = res.getColor(R.color.time_picker_text);

		mTextSizeLarge = res.getDimension(R.dimen.time_picker_number_large);
		mTextSizeSmall = res.getDimension(R.dimen.time_picker_number_small);

		mTextPaddingRight = (int) res.getDimension(R.dimen.time_picker_text_padding_right);
		mTextPaddingBottom = (int) res.getDimension(R.dimen.time_picker_text_padding_bottom);
		mPt = new Paint();
		mPt.setTextAlign(Paint.Align.RIGHT);

		mDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				setCurrentStep(Math.round(e.getX() / mStepWidth));
				tryFireListener();
				return super.onSingleTapConfirmed(e);
			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				setCurrentStep(Math.round(e2.getX() / mStepWidth));
				tryFireListener();
				return true;
			}

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				setCurrentStep(Math.round(e.getX() / mStepWidth * 4 / mTotalStep) * mTotalStep / 4);
				tryFireListener();
				return true;
			}
		});
	}

	public void setOnSliderChangedListener(OnSliderChangedListener l) {
		this.mListener = l;
	}

	public void bindTextView(TextView tv) {
		this.mBoundTv = tv;
	}

	public void setFormat(String format) {
		this.mFormat = format;
	}

	private void tryFireListener() {
		if (mLastPos != mCurrentPos) {
			if (mListener != null) {
				mListener.onSliderChanged(mCurrentPos);
			}
			if (mBoundTv != null) {
				mBoundTv.setText(mFormat == null ? String.valueOf(mCurrentPos) : String.format(mFormat, mCurrentPos));
			}
			performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			mLastPos = mCurrentPos;
		}
	}

	public void setStep(int total, int min, int max) {
		this.mTotalStep = total;
		this.mMinStep = Math.max(0, min);
		this.mMaxStep = Math.min(total, max);
		updateStepWidth();
		invalidate();
	}

	public void setCurrentStep(int current) {
		this.mCurrentPos = Math.max(Math.min(current, mMaxStep), mMinStep);
		if (mLastPos != mCurrentPos) {
			performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		}
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mDetector.onTouchEvent(event);
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			isPressed = true;
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			isPressed = false;
			break;
		}
		invalidate();
		return true;
	}

	private int getPosition(int step) {
		return (int) (step * mStepWidth);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		mPt.setColor(mColorBgUnavailable);
		canvas.drawRect(getPaddingTop(), getPaddingTop() + mLineLengthLong, mWidth, getPaddingTop() + mHeight, mPt);
		mPt.setColor(isPressed ? mColorBgPressed : mColorBg);
		canvas.drawRect(getPosition(mMinStep), getPaddingTop() + mLineLengthLong, getPosition(mMaxStep),
				getPaddingTop() + mHeight, mPt);
		int quarter = mTotalStep / 4;
		for (int i = 1; i < 4; i++) {
			mPt.setColor(mColorLine);
			canvas.drawLine(getPosition(quarter * i), i % 2 == 0 ? getPaddingTop() : getPaddingTop() + mLineLengthLong
					- mLineLengthShort, getPosition(quarter * i), getPaddingTop() + mLineLengthLong, mPt);
			mPt.setColor(mColorText);
			mPt.setTextSize(i % 2 == 0 ? mTextSizeLarge : mTextSizeSmall);
			canvas.drawText(String.valueOf(quarter * i), getPosition(quarter * i) - mTextPaddingRight, getPaddingTop()
					+ mLineLengthLong - mTextPaddingBottom, mPt);
		}
		mPt.setColor(mColorLine);
		canvas.drawLine(getPosition(mMaxStep), getPaddingTop(), getPosition(mMaxStep), getPaddingTop()
				+ mLineLengthLong, mPt);
		mPt.setColor(mColorText);
		mPt.setTextSize(mTextSizeLarge);
		canvas.drawText(String.valueOf(mMaxStep), getPosition(mMaxStep) - mTextPaddingRight, getPaddingTop()
				+ mLineLengthLong - mTextPaddingBottom, mPt);

		// canvas.drawLine(getPosition(mCurrentPos), 0,
		// getPosition(mCurrentPos),
		// mHeight, mPt);
		mPt.setColor(mColorBgSelected);
		canvas.drawRect(getPosition(mMinStep), getPaddingTop() + mLineLengthLong, getPosition(mCurrentPos),
				getPaddingTop() + mHeight, mPt);
		super.onDraw(canvas);
	}

	private void updateStepWidth() {
		if (mTotalStep > 0) {
			this.mStepWidth = (float) mWidth / mTotalStep;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		this.mWidth = MeasureSpec.getSize(widthMeasureSpec);
		this.mHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
		updateStepWidth();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public interface OnSliderChangedListener {
		public void onSliderChanged(int pos);
	}

}
