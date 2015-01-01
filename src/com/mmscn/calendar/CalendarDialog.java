package com.mmscn.calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.text.format.Time;
import android.view.View;
import android.widget.ViewFlipper;

import com.mmscn.calendar.DateFlipperController.OnDialogTitleChangeListener;
import com.mmscn.calendar.YearSlideBar.OnYearChangedListener;
import com.mmscn.mmsdialog.MMSDialog;
import com.mmscn.widgets.R;

public class CalendarDialog {
	public static class Builder implements DialogInterface.OnClickListener, OnYearChangedListener,
			OnDialogTitleChangeListener {
		private MMSDialog dialog;
		private Context mContext;

		private YearSlideBar mYearBar;
		private OnDateSetListener mListener;
		private DateFlipperController mController;

		private int mYear, mMonth, mDay;
		private View mContentView;

		public Builder(Context context) {
			this.mContext = context;
			dialog = new MMSDialog(context);
			dialog.setPositiveButton("确定", this).setNeutralButton("今天", this).setNegativeButton("取消", this);
			mContentView = View.inflate(context, R.layout.dialog_calendar, null);
			dialog.setTitle("日期选择");
		}

		/**
		 * 创建一个AlertDialog
		 * 
		 * @return dialog
		 */
		public Dialog create() {
			dialog.setContentView(mContentView);

			dialog.setOnShowListener(new OnShowListener() {
				// 设置在点击“今天”按钮的时候，dialog中的view显示系统当前时间
				@Override
				public void onShow(DialogInterface dialogInterface) {

					// dialog.getButton(DialogInterface.BUTTON_NEUTRAL)
					// .setOnClickListener(new View.OnClickListener() {
					//
					// @Override
					// public void onClick(View v) {
					// Time t = new Time(Time.getCurrentTimezone());
					// t.setToNow();
					// mYear = t.year;
					// mMonth = t.month;
					// mDay = t.monthDay;
					// mController.selectTime(t.year, t.month,
					// t.monthDay);
					// mYearBar.scrollToYear(mYear, true);
					// }
					// });
					mYearBar = (YearSlideBar) mContentView.findViewById(R.id.year_bar);
					mYearBar.scrollToYear(mYear, true);
					mYearBar.setOnYearChangedListener(mController = new DateFlipperController(mContext)
							.setFlipper((ViewFlipper) mContentView.findViewById(R.id.flipper))
							.setOnYearChangedListener(Builder.this).setOnDialogTitleChangeListener(Builder.this)
							.setTime(mYear, mMonth, mDay));

					mController.selectTime(mYear, mMonth, mDay);
				}
			});
			dialog.setTitle(String.format(mContext.getResources().getString(R.string.dialog_title), mYear, mMonth + 1,
					mDay));
			return dialog.getDialog();
		}

		public Builder setOnDateSetLisener(OnDateSetListener l) {
			this.mListener = l;
			return this;
		}

		/**
		 * 设置屏幕上显示的日期
		 * 
		 * @param year
		 * @param month
		 * @param day
		 * @return
		 */
		public Builder setTime(int year, int month, int day) {
			this.mYear = year;
			this.mMonth = month;
			this.mDay = day;
			if (this.mYearBar != null) {
				mYearBar.scrollToYear(mYear, true);
			}
			if (this.mController != null) {
				mController.setTime(year, month, day);
			}
			return this;
		}

		public Builder setTime(long timeMillis) {
			Time t = new Time(Time.getCurrentTimezone());
			t.set(timeMillis);
			return setTime(t.year, t.month, t.monthDay);
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case AlertDialog.BUTTON_POSITIVE:
				if (mController.isDatePicked() && mListener != null) {
					mYear = mController.getmYear();
					mMonth = mController.getmMonth();
					mDay = mController.getmDay();
					mListener.onDateSet(null, mYear, mMonth, mDay);
				}
				break;
			case AlertDialog.BUTTON_NEUTRAL:
				Time t = new Time(Time.getCurrentTimezone());
				t.setToNow();
				if (mListener != null)
					mListener.onDateSet(null, t.year, t.month, t.monthDay);
				break;
			}
		}

		@Override
		public void onYearChanged(int year) {
			mYearBar.scrollToYear(year, false);

		}

		@Override
		public void setTitle(int year, int month, int day) {
			dialog.setTitle(String.format(mContext.getResources().getString(R.string.dialog_title), year, month + 1,
					day)

			);
		}

	}
}
