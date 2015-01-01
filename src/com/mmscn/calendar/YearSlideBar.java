package com.mmscn.calendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.widget.Scroller;
import android.widget.TextView;

import com.mmscn.widgets.R;

/**
 * 年份滚动条
 * 
 * @author Administrator
 * 
 */
public class YearSlideBar extends TextView {
	private final static int YEAR_TO_STEP_MULTIPLIER = 100;
	public final static int MAX_YEAR = 2020;
	public final static int MIN_YEAR = 1990;
	private final static int MAX_FLING_BY_YEAR = 4;
	private final static float YEARS_PER_SCREEN = 2.2f;
	private int mCurrentStep = 200000;
	private OnYearChangedListener mListener;
	private GestureDetector mDetector;
	private float mStepWidth;
	private boolean isFlinging;
	private Scroller mScroller;
	private String[] mYearStr;
	private int[] mYearPos;
	private int mHeight, mWidth;
	private int mCenterHoriz;
	private Paint mPt;
	private int mColorCenter;
	private int mColorVerge;

	public YearSlideBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		Resources res = context.getResources();
		mColorCenter = res.getColor(R.color.calendar_year_color_center);
		mColorVerge = res.getColor(R.color.calendar_year_color_verge);
		float textSize = res.getDimension(R.dimen.calendar_year_text);

		mPt = new Paint();
		mPt.setTextSize(textSize);
		mPt.setTextAlign(Align.CENTER);
		mPt.setAntiAlias(true);

		mDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

				tryTriggerListener((int) (mCurrentStep + distanceX / mStepWidth));
				mCurrentStep += distanceX / mStepWidth;
				updateDisplayArgs();
				invalidate();
				return true;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				mScroller.abortAnimation();
				mScroller.fling(
						mCurrentStep,
						0,
						(int) (-velocityX / 5 / mStepWidth),
						0,
						Math.max(MIN_YEAR * YEAR_TO_STEP_MULTIPLIER, mCurrentStep - MAX_FLING_BY_YEAR
								* YEAR_TO_STEP_MULTIPLIER),
						Math.min(MAX_YEAR * YEAR_TO_STEP_MULTIPLIER, mCurrentStep + MAX_FLING_BY_YEAR
								* YEAR_TO_STEP_MULTIPLIER), 0, 0);
				isFlinging = true;
				invalidate();
				return true;
			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				int pos = (int) (mCurrentStep + (int) (e.getX() - mCenterHoriz) / mStepWidth);
				if (pos < MAX_YEAR * YEAR_TO_STEP_MULTIPLIER && pos > MIN_YEAR * YEAR_TO_STEP_MULTIPLIER) {
					scrollToYear(getNearestYear(pos), false);
				}
				return super.onSingleTapUp(e);
			}
		});

		mScroller = new Scroller(context);
		updateDisplayArgs();
		setClickable(true);
	}

	public void setOnYearChangedListener(OnYearChangedListener l) {
		this.mListener = l;
	}

	/**
	 * 屏幕显示的年份滚动
	 * 
	 * @param year
	 *            年份
	 * @param skipAnim
	 *            年份是否滚动
	 */
	public void scrollToYear(int year, boolean skipAnim) {
		mScroller.abortAnimation();
		if (skipAnim) {
			this.mCurrentStep = year * YEAR_TO_STEP_MULTIPLIER;
			updateDisplayArgs();
		} else {
			int destStep = year * YEAR_TO_STEP_MULTIPLIER;
			mScroller.startScroll(mCurrentStep, 0, destStep - mCurrentStep, 0);
		}
		invalidate();
	}

	/**
	 * 当手指离开屏幕或手势失败的时候离中心最进的年份滚动到中心位置
	 * 
	 * @param event
	 */
	private void processUp(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			scrollToYear(getNearestYear(mCurrentStep), false);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {

			mScroller.abortAnimation();
			isFlinging = false;
		}
		processUp(event);
		return mDetector.onTouchEvent(event) || super.onTouchEvent(event);
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			int currentStep = mScroller.getCurrX();
			tryTriggerListener(currentStep);
			this.mCurrentStep = currentStep;
			updateDisplayArgs();
			invalidate();
		} else if (isFlinging) {
			scrollToYear(getNearestYear(mCurrentStep), false);
			isFlinging = false;
		}
	}

	/**
	 * 触发年份改变的监听
	 * 
	 * @param step
	 *            x位置
	 */
	private void tryTriggerListener(int step) {
		int year = step / YEAR_TO_STEP_MULTIPLIER;
		if (year != mCurrentStep / YEAR_TO_STEP_MULTIPLIER) {
			if (year <= MAX_YEAR && year >= MIN_YEAR) {
				if (mListener != null) {
					mListener.onYearChanged(year);
				}
			}
			performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		for (int i = 0; i < mYearStr.length; i++) {
			canvas.drawText(mYearStr[i], mYearPos[i], mHeight - getPaddingBottom(), mPt);
		}
		super.onDraw(canvas);
	}

	/**
	 * @param step
	 *            X位置
	 * @return 离中心位置最近的年份
	 */
	private int getNearestYear(int step) {
		int lowerBound = step / YEAR_TO_STEP_MULTIPLIER;
		if (step - lowerBound * YEAR_TO_STEP_MULTIPLIER > YEAR_TO_STEP_MULTIPLIER / 2) {
			return (int) Math.ceil((float) step / YEAR_TO_STEP_MULTIPLIER);
		}
		return lowerBound;
	}

	private void updateDisplayArgs() {
		mCurrentStep = Math.max(Math.min(MAX_YEAR * YEAR_TO_STEP_MULTIPLIER, mCurrentStep), MIN_YEAR
				* YEAR_TO_STEP_MULTIPLIER);
		int year = mCurrentStep / YEAR_TO_STEP_MULTIPLIER;
		this.mYearStr = new String[] { String.valueOf(year - 1), String.valueOf(year),
				String.valueOf(year + 1) };
		int pos = (int) (mCenterHoriz + (-mCurrentStep + mCurrentStep / YEAR_TO_STEP_MULTIPLIER
				* YEAR_TO_STEP_MULTIPLIER)
				* mStepWidth);
		this.mYearPos = new int[] { (int) (pos - YEAR_TO_STEP_MULTIPLIER * mStepWidth), pos,
				(int) (pos + YEAR_TO_STEP_MULTIPLIER * mStepWidth) };
	}

	public interface OnYearChangedListener {
		public void onYearChanged(int year);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		this.mWidth = MeasureSpec.getSize(widthMeasureSpec);
		this.mCenterHoriz = mWidth / 2;
		this.mHeight = MeasureSpec.getSize(heightMeasureSpec);
		this.mStepWidth = (float) mWidth / YEAR_TO_STEP_MULTIPLIER / YEARS_PER_SCREEN;
		if (mPt != null) {
			mPt.setShader(new LinearGradient(0, 0, mWidth, 0, new int[] { mColorVerge, mColorCenter,
					mColorVerge }, new float[] { 0f, 0.5f, 1f }, TileMode.CLAMP));
		}
		updateDisplayArgs();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

}
