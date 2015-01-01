package com.mmscn.calendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Handler;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import com.mmscn.widgets.R;


public class MonthView extends View {
	private final static int NUM_WEEKS = 6;
	private final static int NUM_DAYS_PER_WEEK = 7;
	private final static int SELECTOR_DELAY = 100;
	private int mHeight, mWidth;
	private long mTime;
	private int[] mDate;
	private int mMonth;
	private boolean[] isCurrenMonth;
	private int mSqHeight, mSqWidth;

	private int mColorBg;
	private int mColorBgOtherMonth;
	private int mColorBgToday;
	private int mColorBgSelector;

	private int mColorText;
	private int mColorTextOtherMonth;

	private int mDatePaddingBottom;

	private int mTodayId = -1;

	private int mSelectedId = -1;
	private int mPressedId;

	private long mMonthStart;

	private Paint mPtBg;
	private Paint mPtLine;
	private Paint mPtText;
	private Paint mPtMonthText;

	private Handler mHandler;

	private GestureDetector mDetector;
	private Runnable mSelectorAnimator;

	private OnMonthActionListener mListener;
	public MonthView(Context context) {
		this(context, null);
	}

	public MonthView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		setToNow();
		// setClickable(true);
	}

	private void init(Context context) {
		Resources res = context.getResources();
		mDatePaddingBottom = (int) res
				.getDimension(R.dimen.calendar_date_padding_bottom);
		float textSize = res.getDimension(R.dimen.calendar_date_text);
		float monthTextSize = res.getDimension(R.dimen.calendar_month_text);

		mColorBg = res.getColor(R.color.calendar_date_bg);
		mColorBgOtherMonth = res.getColor(R.color.calendar_date_bg_other_month);
		mColorBgToday = res.getColor(R.color.calendar_date_bg_today);
		mColorBgSelector = res.getColor(R.color.calendar_date_bg_selector);
		int colorLine = res.getColor(R.color.calendar_date_line);
		int colorMonthText = res.getColor(R.color.calendar_month_text);

		mColorText = res.getColor(R.color.calendar_date_text);
		mColorTextOtherMonth = res
				.getColor(R.color.calendar_date_text_other_month);

		mPtBg = new Paint();
		mPtLine = new Paint();
		mPtText = new Paint();
		mPtMonthText = new Paint();

		mPtLine.setColor(colorLine);
		mPtMonthText.setColor(colorMonthText);
		mPtText.setTextSize(textSize);
		mPtMonthText.setTextSize(monthTextSize);
		mPtText.setTextAlign(Align.CENTER);
		mPtMonthText.setTextAlign(Align.RIGHT);
		mPtText.setAntiAlias(true);
		mPtMonthText.setAntiAlias(true);
		// mPtMonthText.setFakeBoldText(true);
		mPtMonthText.setTextSkewX(-0.2f);

		mHandler = new Handler();

		mDetector = new GestureDetector(context,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onDown(MotionEvent e) {
						mPressedId = getIdFromPosition(e.getX(), e.getY());
						mHandler.removeCallbacks(mSelectorAnimator);
						if (mPressedId >= 0
								&& mPressedId < NUM_DAYS_PER_WEEK * NUM_WEEKS) {
							if (isCurrenMonth[mPressedId])
								mHandler.postDelayed(mSelectorAnimator,
										SELECTOR_DELAY);
							return true;
						} else
							return false;
					}

					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY) {
							if (e1.getX() > e2.getX()) {
								if (mListener != null) {
									mListener.onNext();
								}
							} else {
								if (mListener != null) {
									mListener.onPrevious();
								}
							}
						return true;
					}

					// @Override
					// public boolean onScroll(MotionEvent e1, MotionEvent e2,
					// float distanceX, float distanceY) {
					// if (e2.getX() - e1.getX() > SCROLL_TO_FLIP_THRESHOLD) {
					// onNext();
					// return true;
					// } else if (e1.getX() - e2.getX() >
					// SCROLL_TO_FLIP_THRESHOLD) {
					// onPrevious();
					// return true;
					// }
					// return true;
					// };

					@Override
					public boolean onSingleTapUp(MotionEvent e) {
						int id = getIdFromPosition(e.getX(), e.getY());
						tapOnId(id);
						return super.onSingleTapUp(e);
					}
				});
		mSelectorAnimator = new Runnable() {

			@Override
			public void run() {
				mSelectedId = mPressedId;
				performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				invalidate();
				
			}

		};
	}

	/**
	 * 设置已选择的日期，将获取的日期转化为id，将动画线程添加进消息队列，开启线程
	 * 
	 * @param month
	 * @param day
	 * @return
	 */
	public boolean setSelectedDate(int month, int day) {
		
		int id = getIdFromDate(month, day);
		if (id >= 0 && id < NUM_DAYS_PER_WEEK * NUM_WEEKS) {
			tapOnId(id);
			mPressedId = id;
			mHandler.post(mSelectorAnimator);
			return true;
		}
		return false;
	}

	/**
	 * 通过日期获取id号
	 * 
	 * @param month
	 * @param day
	 * @return
	 */
	private int getIdFromDate(int month, int day) {
		if (month == mMonth) {
			for (int i = 0; i < NUM_DAYS_PER_WEEK * NUM_WEEKS; i++) {
				if (isCurrenMonth[i] && day == mDate[i]) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * 通过手指点击屏幕获取的id来进行日期的选择
	 * 
	 * @param id
	 */
	public void tapOnId(int id) {
		if (id >= 0) {
			if (isCurrenMonth[id]) {
				Time t = new Time(Time.getCurrentTimezone());
				t.set(mMonthStart);
				t.monthDay++;
				t.normalize(true);
				if (mListener != null) {
					Log.i("datepick", String.format("%d year %d month %d day",
							t.year, t.month, t.monthDay));

					mListener.onDateSelected(t.year, t.month, mDate[id]);
				}
			} else if (id < NUM_DAYS_PER_WEEK) {
				if (mListener != null) {
					mListener.onPrevious();
				}
			} else {
				if (mListener != null) {
					mListener.onNext();
				}
			}
			invalidate();
		}
	}

	public MonthView setOnMonthActionListener(OnMonthActionListener l) {
		this.mListener = l;
		return this;
	}
	/**
	 * 设置日期跳转到选中时间页面
	 * 
	 * @param t
	 *            时间
	 */
	public void setTime(long t) {
		this.mTime = t;
		updateDisplayArgs();
		invalidate();
	}
	
	/**
	 * 设置日期选中跳转到系统当前时间页面
	 */
	public void setToNow() {
		setTime(System.currentTimeMillis());
	}

	private int getIdFromPosition(float x, float y) {
		for (int i = 0; i < NUM_DAYS_PER_WEEK * NUM_WEEKS; i++) {
			if (getLeftFromId(i) < x && getLeftFromId(i) + mSqWidth > x
					&& getTopFromId(i) < y && getTopFromId(i) + mSqHeight > y) {
				return i;
			}
		}
		return -1;
	}

	private void updateDisplayArgs() {
		mTodayId = -1;
		mSelectedId = -1;
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		Time t = new Time(Time.getCurrentTimezone());
		t.set(mTime);
		t.monthDay = 1;
		t.hour = 1;
		mMonth = t.month;
		this.mMonthStart = t.normalize(true);
		t.monthDay -= t.weekDay;
		t.normalize(true);
		int[] date = new int[NUM_WEEKS * NUM_DAYS_PER_WEEK];
		boolean[] isCurrentMonth = new boolean[NUM_WEEKS * NUM_DAYS_PER_WEEK];
		for (int i = 0; i < NUM_WEEKS * NUM_DAYS_PER_WEEK; t.monthDay++, t
				.normalize(true), i++) {
			date[i] = t.monthDay;
			isCurrentMonth[i] = t.month == mMonth;
			if (today.year==t.year && today.month==t.month && today.monthDay == t.monthDay) {
				mTodayId = i;
			}
		}
		
		this.mDate = date;
		this.isCurrenMonth = isCurrentMonth;
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		 switch (event.getAction() & MotionEvent.ACTION_MASK) {
//		 case MotionEvent.ACTION_CANCEL:
//		 case MotionEvent.ACTION_UP:
//		 mSelectedId = -1;
//		 mHandler.removeCallbacks(mSelectorAnimator);
//		 invalidate();
//		 break;
//		 }
		return mDetector.onTouchEvent(event) || super.onTouchEvent(event);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int i;

		int left;
		int top;
		for (i = 0; i < NUM_DAYS_PER_WEEK * NUM_WEEKS; i++) {
			left = getLeftFromId(i);
			top = getTopFromId(i);
			mPtBg.setColor(isCurrenMonth[i] ? mColorBg : mColorBgOtherMonth);
			canvas.drawRect(left, top, left + mSqWidth, top + mSqHeight, mPtBg);
		}
		canvas.drawText(String.valueOf(mMonth + 1), mWidth, mHeight - 2,
				mPtMonthText);
		for (i = 0; i < NUM_DAYS_PER_WEEK * NUM_WEEKS; i++) {
			left = getLeftFromId(i);
			top = getTopFromId(i);
			mPtText.setColor(isCurrenMonth[i] ? mColorText
					: mColorTextOtherMonth);
			canvas.drawText(String.valueOf(mDate[i]), left + mSqWidth / 2, top
					+ mSqHeight - mDatePaddingBottom, mPtText);
		}
		for (i = 0; i <= NUM_DAYS_PER_WEEK; i++) {
			canvas.drawLine(getLeftFromColumn(i), 0, getLeftFromColumn(i),
					mHeight, mPtLine);
		}
		for (i = 0; i <= NUM_WEEKS; i++) {
			canvas.drawLine(0, getTopFromRow(i), mWidth, getTopFromRow(i),
					mPtLine);
		}
		if (mTodayId >= 0 && mTodayId < NUM_DAYS_PER_WEEK * NUM_WEEKS) {
			drawDate(mTodayId, isCurrenMonth[i] ? mColorText
					: mColorTextOtherMonth, mColorBgToday, canvas);
		}
		if (mSelectedId >= 0 && mSelectedId < NUM_DAYS_PER_WEEK * NUM_WEEKS) {
			drawDate(mSelectedId, isCurrenMonth[i] ? mColorText
					: mColorTextOtherMonth, mColorBgSelector, canvas);
		}
	}

	private void drawDate(int id, int textColor, int bgColor, Canvas canvas) {
		int left = getLeftFromId(id);
		int top = getTopFromId(id);
		mPtBg.setColor(bgColor);
		mPtText.setColor(textColor);
		canvas.drawRect(left, top, left + mSqWidth, top + mSqHeight, mPtBg);
		canvas.drawText(String.valueOf(mDate[id]), left + mSqWidth / 2, top
				+ mSqHeight - mDatePaddingBottom, mPtText);
	}

	private int getTopFromId(int id) {
		return getTopFromRow(id / NUM_DAYS_PER_WEEK);
	}

	private int getTopFromRow(int row) {
		return row == NUM_WEEKS ? mHeight : row * mSqHeight;
	}

	private int getLeftFromId(int id) {
		return getLeftFromColumn(id % NUM_DAYS_PER_WEEK);
	}

	private int getLeftFromColumn(int column) {
		return column * mSqWidth;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		this.mWidth = MeasureSpec.getSize(widthMeasureSpec);
		this.mHeight = MeasureSpec.getSize(heightMeasureSpec);
		this.mSqHeight = mHeight / NUM_WEEKS;
		this.mSqWidth = mWidth / NUM_DAYS_PER_WEEK;
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public interface OnMonthActionListener {
		public void onNext();

		public void onPrevious();

		public void onDateSelected(int year, int month, int day);
		
	}
}
