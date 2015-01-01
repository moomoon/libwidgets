package com.mmscn.widgets;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import android.widget.ListView;

public class VerticalViewPager extends ListView {

	private FragmentListAdapter mAdapter;
	private OnAdapterChangedListener mListener;

	public VerticalViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected void setOnAdapterChangedListener(OnAdapterChangedListener listener) {
		this.mListener = listener;
	}

	public void setAdapter(FragmentPagerAdapter adapter, FragmentActivity activity) {
		mAdapter = new FragmentListAdapter(adapter, activity);
		setAdapter(mAdapter);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		if (adapter instanceof FragmentListAdapter && null != mListener)
			mListener.onAdapterChanged(((FragmentListAdapter) adapter).getWrappedAdapter());
	}

	protected interface OnAdapterChangedListener {
		public void onAdapterChanged(FragmentPagerAdapter adapter);
	}

}
