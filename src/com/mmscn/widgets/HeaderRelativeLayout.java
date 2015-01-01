package com.mmscn.widgets;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.RelativeLayout;

public class HeaderRelativeLayout extends RelativeLayout implements OnScrollListener {

	private String mHeaderTag;
	private List<Reference<View>> mHeaders;
	private Map<View, int[]> mOriginalPos;

	public HeaderRelativeLayout(Context context) {
		this(context, null);
	}

	public HeaderRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.HeaderLayout);
		mHeaderTag = ta.getString(R.styleable.HeaderLayout_header);
		ta.recycle();
	}

	@Override
	public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, index, params);
		setupView(child);
	}

	private void setupView(View child) {

		if (null != mHeaderTag && mHeaderTag.equals(child.getTag())) {
			if (null == mHeaders)
				mHeaders = new ArrayList<Reference<View>>();
			mHeaders.add(new WeakReference<View>(child));
		}
	}

	private void updateHeader() {
		if (null != mHeaders) {
			final int parentTop = getTop();
			for (Reference<View> ref : mHeaders) {
				View header = ref.get();
				if (null == header)
					continue;
				int[] orig = mOriginalPos.get(header);
				if (null != orig) {
					int top = Math.min(getHeight() - header.getHeight(), Math.max(0, -parentTop));
					header.layout(orig[0], top, orig[2], top + header.getHeight());
				}
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (null != mHeaders) {
			for (Reference<View> ref : mHeaders) {
				View header = ref.get();
				if (null == header)
					continue;
				if (null == mOriginalPos)
					mOriginalPos = new WeakHashMap<View, int[]>();
				mOriginalPos.put(header,
						new int[] { header.getLeft(), header.getTop(), header.getRight(), header.getBottom() });
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		updateHeader();

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

}
