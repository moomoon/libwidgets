package com.mmscn.timepicker;

import java.util.Locale;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.format.Time;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.mmscn.mmsdialog.MMSDialog;
import com.mmscn.widgets.R;
import com.mmscn.timepicker.Slider.OnSliderChangedListener;

public class TimePickerDialog {
	private final static int MIN_HOUR = 0;
	private final static int MAX_HOUR = 23;
	private final static int TOTAL_HOUR = 24;
	private final static int MIN_MINUTE = 0;
	private final static int MAX_MINUTE = 59;
	private final static int TOTAL_MINUTE = 60;
	private final static String TIME_TITLE_FORMAT = "%d : %02d";

	public static class Builder implements DialogInterface.OnClickListener {
		// private AlertDialog.Builder builder;
		private MMSDialog alertDialog;

		private Context mContext;
		private OnTimePickedListener mListener;
		private LinearLayout mExtraBtContainer;
		private Slider mHourSlider;
		private Slider mMinSlider;
		private EditText mHourEditText;
		private EditText mMinuteEditText;
		private View mContentView;
		private int hour, minute;

		public Builder(Context context) {
			this.mContext = context;
			mContentView = View.inflate(context, R.layout.dialog_time_picker, null);
			this.mExtraBtContainer = (LinearLayout) mContentView.findViewById(R.id.time_picker_extra_bt_container);
			this.mHourSlider = (Slider) mContentView.findViewById(R.id.time_picker_slider_hour);
			this.mMinSlider = (Slider) mContentView.findViewById(R.id.time_picker_slider_minute);
			this.mHourEditText = (EditText) mContentView.findViewById(R.id.time_picker_hour);

			this.mMinuteEditText = (EditText) mContentView.findViewById(R.id.time_picker_minute);
			this.alertDialog = new MMSDialog(context).setTitle("设置时间").setPositiveButton("确定", this)
					.setNeutralButton("现在", this).setNegativeButton("取消", this);
			// builder.setView(mContentView);

			mHourSlider.setStep(TOTAL_HOUR, MIN_HOUR, MAX_HOUR);
			mMinSlider.setStep(TOTAL_MINUTE, MIN_MINUTE, MAX_MINUTE);

			mHourSlider.bindTextView(mHourEditText);
			mMinSlider.bindTextView(mMinuteEditText);
			mMinSlider.setFormat("%02d");

			mHourSlider.setOnSliderChangedListener(new OnSliderChangedListener() {

				@Override
				public void onSliderChanged(int pos) {
					hour = pos;
					updateTimeTitle();
				}
			});
			mMinSlider.setOnSliderChangedListener(new OnSliderChangedListener() {

				@Override
				public void onSliderChanged(int pos) {
					minute = pos;
					updateTimeTitle();
				}
			});
			mHourEditText.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					if (mHourEditText.length() > 0) {
						hour = Integer.parseInt(mHourEditText.getText().toString());
					} else {
						hour = 0;
					}
					if (hour > MAX_HOUR || hour < MIN_HOUR) {
						hour = Math.max(Math.min(hour, MAX_HOUR), MIN_HOUR);
						mHourEditText.setText(String.valueOf(hour));
					}
					mHourSlider.setCurrentStep(hour);
					updateTimeTitle();
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub

				}
			});

			mMinuteEditText.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					if (mMinuteEditText.length() > 0) {
						minute = Integer.parseInt(mMinuteEditText.getText().toString());
					} else {
						minute = 0;
					}
					if (minute < MIN_MINUTE || minute > MAX_MINUTE) {
						minute = Math.max(Math.min(minute, MAX_MINUTE), MIN_MINUTE);
						mMinuteEditText.setText(String.valueOf(minute));
					}
					mMinSlider.setCurrentStep(minute);
					updateTimeTitle();
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					// TODO Auto-generated method stub

				}

				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub

				}
			});
		}

		public Builder setExtraItem(String[] items, final int[][] timePoints) {
			// int count = items.length;
			// int numRows = (int) Math.ceil((float) count / numColumns);
			// Resources res = mContext.getResources();
			// int marginLeft = (int) res
			// .getDimension(R.dimen.time_picker_extra_bt_margin_left);
			// int marginTop = (int) res
			// .getDimension(R.dimen.time_picker_extra_bt_margin_top);
			// int marginRight = (int) res
			// .getDimension(R.dimen.time_picker_extra_bt_margin_right);
			// int marginBottom = (int) res
			// .getDimension(R.dimen.time_picker_extra_bt_margin_bottom);
			// float textSize = res
			// .getDimension(R.dimen.time_picker_extra_bt_text);
			// int textColor = res.getColor(R.color.time_picker_extra_bt_text);
			// for (int i = 0; i < numRows; i++) {
			// LinearLayout ll = new LinearLayout(mContext);
			// ll.setWeightSum(numColumns);
			// mExtraBtContainer.addView(ll);
			// for (int j = i * numColumns; j < Math.min(count, i * numColumns
			// + numColumns); j++) {
			// final int id = j;
			// TextView tv = new TextView(mContext);
			// tv.setText(items[id]);
			// tv.setTextSize(textSize);
			// tv.setTextColor(textColor);
			// tv.setGravity(Gravity.CENTER);
			// if (bgRes != 0) {
			// tv.setBackgroundResource(bgRes);
			// }
			// tv.setOnClickListener(new OnClickListener() {
			//
			// @Override
			// public void onClick(View v) {
			// l.onClicked(Builder.this, alertDialog, id);
			//
			// }
			// });
			// ll.addView(tv);
			// LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
			// 0, LinearLayout.LayoutParams.WRAP_CONTENT);
			// lp.weight = 1;
			// lp.gravity = Gravity.CENTER;
			// lp.setMargins(marginLeft, marginTop, marginRight,
			// marginBottom);
			// tv.setLayoutParams(lp);
			// }
			// ll.requestLayout();
			// }
			alertDialog.setExtraButtons(items, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					mListener.onTimePicked(timePoints[which][0], timePoints[which][1]);
				}
			});
			return this;
		}

		public Builder setTitle(CharSequence title) {

			int titleTextColor = mContext.getResources().getColor(R.color.time_picker_title_text);

			alertDialog.setTitle(Html.fromHtml(String.format("<font color='#%h'>%s</font>", titleTextColor & 0xffffff,
					title)));
			return this;
		}

		public Builder setTime(int hour, int minute) {
			this.hour = Math.min(Math.max(hour, MIN_HOUR), MAX_HOUR);
			this.minute = Math.min(Math.max(minute, MIN_MINUTE), MAX_MINUTE);
			updateDisplay();
			return this;
		}

		public Builder setToNow() {
			Time t = new Time(Time.getCurrentTimezone());
			t.setToNow();
			return setTime(t.hour, t.minute);
		}

		private void updateDisplay() {
			mHourSlider.setCurrentStep(hour);
			mMinSlider.setCurrentStep(minute);
			mHourEditText.setText(String.valueOf(hour));
			mMinuteEditText.setText(String.format("%02d", minute));
		}

		public Builder setOnTimePickedLitener(OnTimePickedListener l) {
			this.mListener = l;
			return this;
		}

		public Dialog create() {
			// alertDialog = new MMSDialog(mContext);//.create();
			alertDialog.setContentView(mContentView);
			// alertDialog.setOnShowListener(new OnShowListener() {
			//
			// @Override
			// public void onShow(DialogInterface dialog) {
			// updateDisplay();
			// alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL)
			// .setOnClickListener(new View.OnClickListener() {
			//
			// @Override
			// public void onClick(View v) {
			// setToNow();
			// updateDisplay();
			// }
			// });
			// }
			// });
			return alertDialog.getDialog();
		}

		private void updateTimeTitle() {
			String title = String.format(Locale.CHINESE, TIME_TITLE_FORMAT, hour, minute);
			int titleTextColor = mContext.getResources().getColor(R.color.time_picker_title_text);

			if (alertDialog != null) {
				alertDialog.setTitle(title
				// Html.fromHtml(String.format(
				// "<font color='#%h'>%s</font>", titleTextColor & 0xffffff,
				// title))

						);
			}
			// else if (builder != null) {
			// builder.setTitle(Html.fromHtml(String.format(
			// "<font color='#%h'>%s</font>", titleTextColor & 0xffffff,
			// title)));
			// }
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				if (mListener != null) {
					mListener.onTimePicked(hour, minute);
				}
				break;
			case DialogInterface.BUTTON_NEUTRAL:
				Time t = new Time(Time.getCurrentTimezone());
				t.setToNow();
				if (mListener != null) {
					mListener.onTimePicked(t.hour, t.minute);
				}
				break;
			}
		}
	}

}