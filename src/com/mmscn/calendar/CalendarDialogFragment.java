package com.mmscn.calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.Time;
import android.view.View;
import android.widget.ViewFlipper;

import com.mmscn.calendar.DateFlipperController.OnDialogTitleChangeListener;
import com.mmscn.calendar.YearSlideBar.OnYearChangedListener;
import com.mmscn.mmsdialog.MMSDialog;
import com.mmscn.widgets.R;

public class CalendarDialogFragment extends DialogFragment implements OnClickListener, OnYearChangedListener,
		OnDialogTitleChangeListener {
	private YearSlideBar mYearBar;
	private OnDateSetListener mListener;
	private DateFlipperController mController;
	private MMSDialog dialog;
	private int mYear, mMonth, mDay;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final View v = View.inflate(getActivity(), R.layout.dialog_calendar, null);
		dialog = new MMSDialog(getActivity());
		dialog.setTitle("日期选择");
		dialog.setContentView(v);
		dialog.setPositiveButton("确定", this);
		dialog.setNegativeButton("取消", this);
		dialog.setNeutralButton("今天", this);

		dialog.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialogInterface) {
				// TODO Auto-generated method stub
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
				// dialog.getButton(DialogInterface.BUTTON_NEUTRAL)
				// .setOnClickListener(new View.OnClickListener() {
				//
				// @Override
				// public void onClick(View v) {
				// // TODO Auto-generated method stub
				// Time t = new Time(Time.getCurrentTimezone());
				// t.setToNow();
				// mYear = t.year;
				// mMonth = t.month;
				// mDay = t.monthDay;
				// mController.selectTime(mYear, mMonth, mDay);
				// mYearBar.scrollToYear(mYear, true);
				// }
				// });
				mYearBar = (YearSlideBar) v.findViewById(R.id.year_bar);
				mYearBar.scrollToYear(mYear, true);
				mYearBar.setOnYearChangedListener(mController = new DateFlipperController(getActivity())
						.setFlipper((ViewFlipper) v.findViewById(R.id.flipper))
						.setOnYearChangedListener(CalendarDialogFragment.this)
						.setOnDialogTitleChangeListener(CalendarDialogFragment.this).setTime(mYear, mMonth, mDay));
				mController.selectTime(mYear, mMonth, mDay);

			}
		});

		return dialog.getDialog();
	}

	public CalendarDialogFragment setOnDateSetLisener(OnDateSetListener l) {
		this.mListener = l;
		return this;
	}

	/**
	 * 根据屏幕上左上角的显示来设置时间
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public CalendarDialogFragment setTime(int year, int month, int day) {
		mYear = year;
		mMonth = month;
		mDay = day;
		if (this.mYearBar != null) {
			mYearBar.scrollToYear(mYear, false);
		}
		if (this.mController != null) {
			mController.setTime(year, month, day);
		}
		return this;
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
		dialog.setTitle(String.format(getActivity().getResources().getString(R.string.dialog_title), year, month + 1,
				day));

	}
}
