package com.mmscn.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;

public class TableRadioButton extends RadioButton implements OnClickListener, OnCheckedChangeListener {

	private OnClickListener mInternalOnClickListener;
	private OnCheckedChangeListener mInternalOnCheckedListener;
	private OnClickListener mWrappedOnClickListener;
	private OnCheckedChangeListener mWrappedOnCheckedListener;

	public TableRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		super.setOnClickListener(this);
		super.setOnCheckedChangeListener(this);
	}

	public void setInternalOnClickListener(OnClickListener l) {
		this.mInternalOnClickListener = l;
	}

	public void setInternalOnCheckedListener(OnCheckedChangeListener listener) {
		this.mInternalOnCheckedListener = listener;
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		this.mWrappedOnClickListener = l;
	}

	@Override
	public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
		this.mWrappedOnCheckedListener = listener;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (null != mInternalOnCheckedListener)
			mInternalOnCheckedListener.onCheckedChanged(buttonView, isChecked);
		if (null != mWrappedOnCheckedListener)
			mWrappedOnCheckedListener.onCheckedChanged(buttonView, isChecked);
	}

	@Override
	public void onClick(View v) {
		if (null != mInternalOnClickListener)
			mInternalOnClickListener.onClick(v);
		if (null != mWrappedOnClickListener)
			mWrappedOnClickListener.onClick(v);
	}

}
