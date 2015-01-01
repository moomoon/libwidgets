package com.mmscn.widgets;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;

/**
 * 表格布局形式的RadioGroup
 * 
 * @author joey.jia@mmscn.com
 * 
 */
public class TableRadioGroup extends TableLayout implements OnClickListener,
		android.widget.CompoundButton.OnCheckedChangeListener {

	private RadioButton activeRadioButton;
	private OnCheckedChangeListener mListener;
	private UncheckableOnCheckedChangedListener mUncheckableListener;
	private SparseArray<WeakReference<RadioButton>> mRadioBtMap;
	private final Set<CompoundButton> mCheckLockSet = Collections
			.newSetFromMap(new WeakHashMap<CompoundButton, Boolean>());
	private boolean isUncheckable;

	public TableRadioGroup(Context context) {
		this(context, null);
	}

	public TableRadioGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TableRadioGroup);
		isUncheckable = a.getBoolean(R.styleable.TableRadioGroup_isUncheckable, false);
		a.recycle();
	}

	public void setOnCheckedChangeListener(OnCheckedChangeListener l) {
		this.mListener = l;
	}

	public void setUnchechableListener(UncheckableOnCheckedChangedListener l) {
		this.mUncheckableListener = l;
	}

	public int idToPosition(int id) {
		return mRadioBtMap.indexOfKey(id);
	}

	public int positionToId(int pos) {
		return mRadioBtMap.keyAt(pos);
	}

	public final void setUncheckable(boolean isUncheckable) {
		this.isUncheckable = isUncheckable;
	}

	@Override
	public void onClick(View v) {
		Log.e("onCheckedChanged", "onClick checked = " + ((CompoundButton) v).isChecked());
		if (mCheckLockSet.contains(v)) {
			mCheckLockSet.remove(v);
		} else {
			final RadioButton rb = (RadioButton) v;
			if (rb.isChecked()) {
				if (isUncheckable)
					rb.setChecked(false);
			} else {
				rb.setChecked(true);
			}
		}
	}

	@Override
	public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, index, params);
		setUpRadioButtons((TableRow) child);
	}

	public void clearCheck() {
		if (null != activeRadioButton) {
			activeRadioButton.setChecked(false);
			activeRadioButton = null;
		}
	}

	public void check(int id) {
		if (null == mRadioBtMap)
			return;
		WeakReference<RadioButton> ref = mRadioBtMap.get(id);
		if (null == ref) {
			clearCheck();
			return;
		}
		RadioButton rb = ref.get();
		if (null == rb)
			return;
		rb.setChecked(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.TableLayout#addView(android.view.View,
	 * android.view.ViewGroup.LayoutParams)
	 */
	@Override
	public void addView(View child, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, params);
		setUpRadioButtons((TableRow) child);
	}

	private void setUpRadioButtons(TableRow tr) {
		if (null == mRadioBtMap)
			mRadioBtMap = new SparseArray<WeakReference<RadioButton>>();
		final int c = tr.getChildCount();
		for (int i = 0; i < c; i++) {
			final View v = tr.getChildAt(i);
			if (v instanceof RadioButton) {
				if (v instanceof TableRadioButton) {
					((TableRadioButton) v).setInternalOnClickListener(this);
					((TableRadioButton) v).setInternalOnCheckedListener(this);
				} else {
					v.setOnClickListener(this);
					((RadioButton) v).setOnCheckedChangeListener(this);
				}
				mRadioBtMap.put(v.getId(), new WeakReference<RadioButton>((RadioButton) v));
			}
		}
	}

	public int getCheckedRadioButtonId() {
		if (activeRadioButton != null) {
			return activeRadioButton.getId();
		}

		return -1;
	}

	public interface UncheckableOnCheckedChangedListener {
		public void onCheckedChanged(TableRadioGroup group, int id, boolean isChecked);
	}

	@Override
	public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
		Log.e("onCheckedChanged", "isChecked = " + isChecked);
		mCheckLockSet.add(buttonView);
		post(new Runnable() {

			@Override
			public void run() {
				mCheckLockSet.remove(buttonView);

			}
		});
		if (isChecked) {
			if (null != activeRadioButton && activeRadioButton != buttonView)
				activeRadioButton.setChecked(false);
			activeRadioButton = (RadioButton) buttonView;
			if (null != mListener) {
				mListener.onCheckedChanged(null, buttonView.getId());
			}
		} else {
			if (activeRadioButton == buttonView)
				activeRadioButton = null;
		}
		if (null != mUncheckableListener) {
			mUncheckableListener.onCheckedChanged(this, buttonView.getId(), isChecked);
		}

	}
}