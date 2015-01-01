package com.mmscn.timepicker;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.mmscn.mmsdialog.MMSDialog;
import com.mmscn.widgets.R;

public class TimeKeyboardDialog extends MMSDialog {

	private final int[] DISPLAYER_ID = new int[] { R.id.time_keyboard_digit_0,
			R.id.time_keyboard_digit_1, R.id.time_keyboard_digit_2,
			R.id.time_keyboard_digit_3 };

	private final TextView[] DISPLAYER;

	private final int[] BUTTON_ID = new int[] { R.id.time_keyboard_button_0,
			R.id.time_keyboard_button_1, R.id.time_keyboard_button_2,
			R.id.time_keyboard_button_3, R.id.time_keyboard_button_4,
			R.id.time_keyboard_button_5, R.id.time_keyboard_button_6,
			R.id.time_keyboard_button_7, R.id.time_keyboard_button_8,
			R.id.time_keyboard_button_9, R.id.time_keyboard_button_00,
			R.id.time_keyboard_button_30 };

	private final int[][] DIGITS = new int[][] { new int[] { 0 },
			new int[] { 1 }, new int[] { 2 }, new int[] { 3 }, new int[] { 4 },
			new int[] { 5 }, new int[] { 6 }, new int[] { 7 }, new int[] { 8 },
			new int[] { 9 }, new int[] { 0, 0 }, new int[] { 0, 3 } };

	protected int[] mInputDigits;
	private OnTimePickedListener mListener;

	private List<View> mViewList;

	private Button positiveButton;

	private OnShowListener mOnShowListener;

	public TimeKeyboardDialog(Context context) {
		super(context);

		View v = null;

		mViewList = new ArrayList<View>();
		setContentView(
				v = View.inflate(context, R.layout.dialog_keyboard, null),
				new LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.WRAP_CONTENT));

		for (int i = 0; i < Math.min(BUTTON_ID.length, DIGITS.length); i++) {
			View button = v.findViewById(BUTTON_ID[i]);
			mViewList.add(button);
			android.view.View.OnClickListener l = new TimeKeyboardClickListener(
					DIGITS[i]);
			button.setOnClickListener(l);
			button.setTag(l);
		}
		DISPLAYER = new TextView[DISPLAYER_ID.length];
		for (int i = 0; i < DISPLAYER_ID.length; i++) {
			DISPLAYER[i] = (TextView) v.findViewById(DISPLAYER_ID[i]);
		}

		mInputDigits = new int[] { -1, -1, -1, -1 };

		v.findViewById(R.id.time_keyboard_backspace).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						for (int i = 0; i < mInputDigits.length - 1; i++) {
							mInputDigits[i] = mInputDigits[i + 1];
						}
						mInputDigits[mInputDigits.length - 1] = -1;
						updateUI();
					}
				});

		DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					if (null == mListener)
						return;
					float dummy = getFakeTime(mInputDigits);
					if (dummy >= 0) {
						int hour = (int) dummy;
						int minute = (int) ((dummy - hour) * 60);
						mListener.onTimePicked(hour, minute);
					}
					break;

				default:
					break;
				}
			}
		};

		setNegativeButton("取消", l);

		setPositiveButton("确定", l);
		positiveButton = getButton(DialogInterface.BUTTON_POSITIVE);

		super.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				Log.i("keyboard", "on show");
				updateUI();
				if (null != mOnShowListener)
					mOnShowListener.onShow(dialog);
			}
		});

	}

	@Override
	public MMSDialog setOnShowListener(OnShowListener l) {
		this.mOnShowListener = l;
		return this;
	}

	private int validUnits() {
		int cnt = 0;
		for (int i = mInputDigits.length - 1; i >= 0; i--) {
			if (mInputDigits[i] <= 0)
				cnt++;
			else
				break;
		}
		return cnt;
	}

	private static boolean isDigitsValid(int[] digits) {
		if (digits.length != 4) {

			Log.e("getlength", "invalid digits ");
			return false;
		}
		float dummy = getFakeTime(digits);

		return dummy >= 0f && dummy < 24f;
	}

	protected static float getFakeTime(int[] digits) {
		float dummy = 0f;
		if (digits[3] > 2)
			return -1f;
		if (digits[3] < 0 && digits[2] < 0 && digits[1] < 0 && digits[0] < 0)
			return -1f;
		dummy += Math.max(0f, (float) digits[3]) * 10f;
		if (digits[2] > (dummy < 20 ? 9 : 4))
			return -1f;
		dummy += Math.max(0f, (float) digits[2]);
		if (digits[1] > (dummy < 2 ? 9 : 5))
			return -1f;
		dummy += Math.max(0f, (float) digits[1]) / 6f;
		if (digits[0] > 9)
			return -1f;
		dummy += Math.max(0f, (float) digits[0]) / 60f;
		return dummy;
	}

	private int[] input(int[] digits) {
		return input(digits, mInputDigits);
	}

	private int[] input(int[] digits, int[] inputDigits) {
		int[] result = new int[inputDigits.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = i < digits.length ? digits[i] : mInputDigits[i
					- digits.length];
		}
		return result;
	}

	private void updateUI() {

		for (View button : mViewList) {
			Object l = button.getTag();
			button.setEnabled(null != l
					&& l instanceof TimeKeyboardClickListener
					&& ((TimeKeyboardClickListener) l).check());
		}
		for (int i = Math.min(DISPLAYER.length, mInputDigits.length) - 1; i >= 0; i--) {
			DISPLAYER[i].setText(mInputDigits[i] < 0 ? "-" : String
					.valueOf(mInputDigits[i]));

		}
		onInputChanged();
	}

	protected void onInputChanged() {
		setTitle(getTimeTitle());
	}

	private String getTimeTitle() {
		float dummy = getFakeTime(mInputDigits);
		if (dummy >= 0) {
			int hour = (int) dummy;
			int minute = (int) ((dummy - hour) * 60);
			return String.format("%d:%02d", hour, minute);
		} else
			return "请选择时间";
	}

	public TimeKeyboardDialog setOnTimePickedListener(OnTimePickedListener l) {
		this.mListener = l;
		return this;
	}

	private class TimeKeyboardClickListener implements View.OnClickListener {

		private int[] digits;

		public TimeKeyboardClickListener(int[] digits) {
			this.digits = digits;
		}

		@Override
		public void onClick(View v) {
			if (tryInput())
				updateUI();

		}

		private boolean check() {
			if (null == digits) {
				return false;
			}
			if (digits.length > validUnits()) {
				return false;
			}
			if (digits.length > 1) {
				float dummy = getFakeTime(mInputDigits);
				if (dummy <= 0 || dummy >= 24) {
					return false;
				}
			} else {
				if (mInputDigits[0] > 5 && digits[0] > 5)
					return false;
			}
			boolean a = isDigitsValid(input(digits));

			return (isDigitsValid(input(digits)));
			// return true;
		}

		private boolean tryInput() {
			if (null == digits)
				return false;
			if (digits.length > validUnits())
				return false;
			int[] result = input(digits);
			if (isDigitsValid(result)) {
				mInputDigits = result;
				return true;
			}
			return false;
		}

	}

}
