package com.mmscn.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mmscn.widgets.VerticalViewPager.OnAdapterChangedListener;

public class VerticalViewPagerIndicator extends ListView implements OnAdapterChangedListener, OnScrollListener {

	private int mCurrentSelectedPosition = -1;
	private VerticalViewPager mPager;
	private Context mContext;
	private FragmentPagerAdapter mWrappedAdapter;
	private int mBgRes;
	private int mTextColorRes;
	private IndicatorAdapter mAdapter;
	private int mScrollState;
	private int mTextSize;
	private int mPadding;

	public VerticalViewPagerIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VerticalViewPagerIndicator);

		mBgRes = a.getResourceId(R.styleable.VerticalViewPagerIndicator_indicatorBackground, 0);
		mTextColorRes = a.getResourceId(R.styleable.VerticalViewPagerIndicator_indicatorTextColor, 0);
		mTextSize = a.getDimensionPixelSize(R.styleable.VerticalViewPagerIndicator_indicatorTextSize, 20);
		mPadding = a.getDimensionPixelSize(R.styleable.VerticalViewPagerIndicator_indicatorPadding, 5);
		a.recycle();
		this.mContext = context;
		mAdapter = new IndicatorAdapter();
		setAdapter(mAdapter);
	}

	public void setVerticalViewPager(VerticalViewPager pager) {
		this.mPager = pager;
		pager.setOnAdapterChangedListener(this);
		pager.setOnScrollListener(this);
		ListAdapter adapter = pager.getAdapter();
		if (null != adapter) {
			if (adapter instanceof FragmentListAdapter)
				onAdapterChanged(((FragmentListAdapter) adapter).getWrappedAdapter());
		}
		forceUpdate();
	}

	@Override
	public void onAdapterChanged(FragmentPagerAdapter adapter) {
		this.mWrappedAdapter = adapter;
		forceUpdate();
	}

	public void forceUpdate() {
		int scrollStateTemp = mScrollState;
		mScrollState = SCROLL_STATE_TOUCH_SCROLL;
		onScroll(mPager, mPager.getFirstVisiblePosition(), mPager.getChildCount(), mPager.getCount());
		mScrollState = scrollStateTemp;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		int lastVisibleItem = firstVisibleItem + visibleItemCount - 1;
		if (mScrollState != SCROLL_STATE_IDLE && lastVisibleItem != mCurrentSelectedPosition) {
			mCurrentSelectedPosition = lastVisibleItem;
			updateIndicators();
			smoothScrollToPosition(mCurrentSelectedPosition);
			if (mScrollState == -1)
				mScrollState = SCROLL_STATE_IDLE;

		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		this.mScrollState = scrollState;
	}

	private void updateIndicators() {
		mAdapter.notifyDataSetChanged();
	}

	private class IndicatorAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return null == mWrappedAdapter ? 0 : mWrappedAdapter.getCount();
		}

		@Override
		public Object getItem(int position) {
			return mWrappedAdapter.getPageTitle(position);
		}

		@Override
		public long getItemId(int position) {
			return mWrappedAdapter.getItemId(position);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			FrameLayout container = null;
			TextView tv = null;
			if (null == convertView) {
				tv = new TextView(mContext);
				tv.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				container = new FrameLayout(mContext);
				container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				container.setTag(tv);
				container.addView(tv);

				tv.setBackgroundResource(mBgRes);
				tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
				tv.setPadding(0, mPadding, 0, mPadding);
				tv.setGravity(Gravity.CENTER);
				if (0 != mTextColorRes)
					tv.setTextColor(mContext.getResources().getColorStateList(mTextColorRes));
			} else {
				container = (FrameLayout) convertView;
				tv = (TextView) container.getTag();
			}
			tv.setSelected(position == mCurrentSelectedPosition);
			tv.setText((CharSequence) getItem(position));
			tv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mPager.setSelection(position);
					mCurrentSelectedPosition = position;
					updateIndicators();
				}
			});
			return container;
		}
	}

}
