package com.mmscn.widgets;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.mmscn.mmsdialog.MMSDialog;

public class MMSSpinner extends TextView {
	private OnClickListener mClickListener;
	private MMSOnItemSelectedListener mItemSelectedListener;
	private CharSequence[] mEntries;

	private static final int[] HAS_CONTENT_STATE_SET = { R.attr.state_has_content };

	public MMSSpinner(Context context) {
		this(context, null);
	}

	public MMSSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MMSSpinner);
		mEntries = ta.getTextArray(R.styleable.MMSSpinner_entries);
		ta.recycle();
		setEnabled(null != mEntries && mEntries.length > 0);
		super.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				List<String> l = new ArrayList<String>();
				for (CharSequence cs : mEntries) {
					if (cs.length() == 0)
						l.add("--");
					else
						l.add(new StringBuilder(cs).toString());
				}
				final String[] array = new String[l.size()];
				l.toArray(array);
				new MMSDialog(getContext()).setExtraButtons(array, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String item = array[which].equals("--") ? "" : array[which];
						setText(item);
						if (null != mItemSelectedListener) {
							mItemSelectedListener.onItemSelected(item);
						}
						dialog.dismiss();
					}

				}).show();
				if (null != mClickListener) {
					mClickListener.onClick(MMSSpinner.this);
				}
			}
		});
		if (null == getBackground())
			setBackgroundResource(R.drawable.simple_spinner_bg);

	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		this.mClickListener = l;
	};

	public void setOnItemSelectedListener(MMSOnItemSelectedListener l) {
		this.mItemSelectedListener = l;
	}

	public void setEntries(String[] entries) {
		this.mEntries = entries;
		setEnabled(null != entries && entries.length > 0);
	}

	public interface MMSOnItemSelectedListener {
		public void onItemSelected(String item);
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		refreshDrawableState();
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		try {
			if (length() > 0) {
				mergeDrawableStates(drawableState, HAS_CONTENT_STATE_SET);
			}
		} catch (NullPointerException e) {
			// we have to wait for mText
		}
		return drawableState;
	}

}
