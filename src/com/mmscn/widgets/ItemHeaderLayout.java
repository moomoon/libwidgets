package com.mmscn.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;

public class ItemHeaderLayout extends FrameLayout implements OnScrollListener {
	private View mHeader;
	private int mOriginalHeaderTop = 0;

	public ItemHeaderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ItemHeaderLayout);
		final int headerId = ta.getResourceId(R.styleable.ItemHeaderLayout_headerView, 0);
		if (0 != headerId) {
			post(new Runnable() {

				@Override
				public void run() {
					mHeader = findViewById(headerId);
					updateHeader();
				}
			});
		}
		ta.recycle();
	}

	public void setHeader(View header) {
		if (mHeader != header) {
			this.mHeader = header;
			requestLayout();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		updateHeader();
	}

	private void updateHeader() {
		if (null != mHeader) {
			final int top = getTop();
			final int headerHeight = mHeader.getMeasuredHeight();
			int currentHeaderTop = mHeader.getTop();
			int toHeaderTop = (int) Math.min(Math.max(-top, mOriginalHeaderTop), getMeasuredHeight() - headerHeight);
			mHeader.offsetTopAndBottom(toHeaderTop - currentHeaderTop);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (null != mHeader) {
			mOriginalHeaderTop = mHeader.getTop();
		}
		updateHeader();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		updateHeader();
	}

}
