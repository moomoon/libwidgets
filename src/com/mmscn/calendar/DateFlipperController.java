package com.mmscn.calendar;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.ViewFlipper;

import com.mmscn.calendar.MonthView.OnMonthActionListener;
import com.mmscn.calendar.YearSlideBar.OnYearChangedListener;
import com.mmscn.widgets.R;

public class DateFlipperController implements OnMonthActionListener, OnYearChangedListener {
	private ViewFlipper mFlipper;
	private MonthView[] mMonthViews;
	private int mCurrentPosition;
	private int year, month, monthDay;
	private OnYearChangedListener mListener;
	private Context mContext;
	private int mYear, mMonth, mDay;
	private boolean isDatePicked = false;
	private OnDialogTitleChangeListener mOnDialogTitleChangeListener;

	public DateFlipperController(Context context) {
		this.mContext = context;
		mMonthViews = new MonthView[] { new MonthView(mContext).setOnMonthActionListener(this),
				new MonthView(mContext).setOnMonthActionListener(this),
				new MonthView(mContext).setOnMonthActionListener(this) };
	}

	/**
	 * 设置时间为系统当前时间
	 * 
	 * @return 时间滑动控制器
	 */
	public DateFlipperController setToNow() {
		return setTime(System.currentTimeMillis());
	}

	public DateFlipperController setOnYearChangedListener(OnYearChangedListener l) {
		this.mListener = l;
		return this;
	}

	public DateFlipperController setOnDialogTitleChangeListener(OnDialogTitleChangeListener l) {
		this.mOnDialogTitleChangeListener = l;
		return this;
	}

	public void selectTime(int year, int month, int day) {
		setTime(year, month, day);
		// mMonthViews[mCurrentPosition].setSelectedDate(month, day);
	}

	/**
	 * 设置时间
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public DateFlipperController setTime(int year, int month, int day) {
		Time t = new Time(Time.getCurrentTimezone());
		t.year = year;
		t.month = month;
		t.monthDay = day;
		setTime(t.normalize(true));
		return this;
	}

	/**
	 * 设置显示时间
	 * 
	 * @param time
	 * @return 日期滑动控制器
	 */
	public DateFlipperController setTime(long time) {
		Time t = new Time(Time.getCurrentTimezone());
		t.set(time);
		this.year = t.year;
		this.month = t.month;
		this.monthDay = t.monthDay;

		updateViews();
		return this;
	}

	/**
	 * 给页面滑动控件添加视图
	 * 
	 * @param flipper
	 *            页面转换滑动控件
	 * @return 日期滑动控制器
	 */
	public DateFlipperController setFlipper(ViewFlipper flipper) {
		this.mFlipper = flipper;
		for (View v : mMonthViews) {
			mFlipper.addView(v);
		}
		return this;
	}

	private void updateViews() {
		Time t = new Time(Time.getCurrentTimezone());
		t.year = year;
		t.month = month - 1;
		t.monthDay = 1;
		Log.i("updateViews", "month = " + t.month);
		int start = mCurrentPosition + mMonthViews.length - 1;
		for (int i = 0; i < mMonthViews.length; i++, t.month++) {
			mMonthViews[start++ % mMonthViews.length].setTime(t.normalize(true));
		}
		if (!isDatePicked) {
			mDay = monthDay;
			mMonthViews[mCurrentPosition].setSelectedDate(month, mDay);
		} else
			mMonthViews[mCurrentPosition].setSelectedDate(month, mDay);

	}

	private void go(int month) {
		this.month += month;
		Time t = new Time(Time.getCurrentTimezone());
		t.year = year;
		t.month = this.month;
		t.normalize(true);
		if (this.year != t.year) {
			if (mListener != null) {
				mListener.onYearChanged(t.year);
			}
		}
		this.year = t.year;
		this.month = t.month;
	}

	private void next() {

		mFlipper.setInAnimation(mContext, R.anim.right_in);
		mFlipper.setOutAnimation(mContext, R.anim.left_out);
		mFlipper.showNext();
		mCurrentPosition++;
		mCurrentPosition %= mMonthViews.length;
		go(1);
		updateViews();
	}

	@Override
	public void onNext() {
		if (this.year < YearSlideBar.MAX_YEAR && this.year >= YearSlideBar.MIN_YEAR) {
			next();
		} else if (this.year == YearSlideBar.MAX_YEAR && this.month < 11) {
			next();
		}
	}

	private void previous() {
		mFlipper.setInAnimation(mContext, R.anim.left_in);
		mFlipper.setOutAnimation(mContext, R.anim.right_out);
		mFlipper.showPrevious();
		mCurrentPosition--;
		mCurrentPosition += mMonthViews.length;
		mCurrentPosition %= mMonthViews.length;
		go(-1);
		updateViews();
	}

	@Override
	public void onPrevious() {
		if (this.year <= YearSlideBar.MAX_YEAR && this.year > YearSlideBar.MIN_YEAR) {
			previous();
		} else if (this.year == YearSlideBar.MIN_YEAR && this.month >= 1) {
			previous();
		}
	}

	@Override
	public void onDateSelected(int year, int month, int day) {
		mYear = year;
		mMonth = month;
		mDay = day;
		if (mOnDialogTitleChangeListener != null) {
			mOnDialogTitleChangeListener.setTitle(mYear, mMonth, mDay);
		}
		isDatePicked = true;
	}

	@Override
	public void onYearChanged(int year) {
		this.year = year;
		updateViews();
	}

	public int getmYear() {
		return mYear;
	}

	public void setmYear(int mYear) {
		this.mYear = mYear;
	}

	public int getmMonth() {
		return mMonth;
	}

	public void setmMonth(int mMonth) {
		this.mMonth = mMonth;
	}

	public int getmDay() {
		return mDay;
	}

	public void setmDay(int mDay) {
		this.mDay = mDay;
	}

	public boolean isDatePicked() {
		return isDatePicked;
	}

	public void setDatePicked(boolean isDatePicked) {
		this.isDatePicked = isDatePicked;
	}

	public interface OnDialogTitleChangeListener {
		public void setTitle(int year, int month, int day);
	}
}
