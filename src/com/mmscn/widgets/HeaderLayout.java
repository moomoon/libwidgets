package com.mmscn.widgets;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.mmscn.widgets.EventScrollView.OnScrollEventListener;

public class HeaderLayout extends RelativeLayout implements OnScrollEventListener {
	private final static int AGGRESSIVE_DELAY = 10;

	public enum WatchMode {
		Passive, Aggressive
	};

	private WatchMode mWatchMode;
	private List<Reference<View>> mHeaders;
	private Map<View, int[]> mLayoutMap;
	private String mHeaderTag = "";
	private View mScrollSrc;
	private int l = 0, t = 0, oldl = 0, oldt = 0, width = 0, height = 0;
	private boolean mAggressiveWatchStarted = false;
	private Handler mHandler = new Handler(Looper.getMainLooper());
	private final Runnable mAggressiveWatcher = new Runnable() {

		@Override
		public void run() {
			if (null == mScrollSrc) {
				mWatchMode = WatchMode.Passive;
			} else {
				passiveScroll(mScrollSrc.getScrollX(), mScrollSrc.getScrollY(), HeaderLayout.this.oldl,
						HeaderLayout.this.oldt, mScrollSrc.getWidth(), mScrollSrc.getHeight());
				mHandler.post(this);
			}
		}
	};
	private final Runnable mAggressivePurger = new Runnable() {

		@Override
		public void run() {
			mHandler.removeCallbacks(mAggressiveWatcher);
			mAggressiveWatchStarted = false;
		}
	};

	public HeaderLayout(Context context) {
		this(context, null);
	}

	public HeaderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.HeaderLayout);
		mHeaderTag = ta.getString(R.styleable.HeaderLayout_header);
		int watchParent = ta.getInteger(R.styleable.HeaderLayout_watchParent, -1);
		ta.recycle();
		switch (watchParent) {
		case 0:
			mWatchMode = WatchMode.Passive;
			break;
		case 1:
			mWatchMode = WatchMode.Aggressive;
			break;
		default:
			break;
		}
		if (-1 != watchParent) {
			post(new Runnable() {

				@Override
				public void run() {
					View parent = (View) getParent();
					mScrollSrc = parent;

				}
			});
		}
		setClipToPadding(false);
	}

	public void setWatchMode(WatchMode mode) {
		this.mWatchMode = mode;
	}

	public void setScrollSrc(View src) {
		this.mScrollSrc = src;
	}

	@Override
	public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, index, params);
		setupView(child);
	}

	@Override
	public void addView(View child, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, params);
	}

	private void setupView(View child) {

		if (null != mHeaderTag && mHeaderTag.equals(child.getTag())) {
			if (null == mHeaders)
				mHeaders = new ArrayList<Reference<View>>();
			mHeaders.add(new WeakReference<View>(child));
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (null != mHeaders) {
			for (Reference<View> ref : mHeaders) {
				View header = ref.get();
				if (null != header) {
					if (null == mLayoutMap)
						mLayoutMap = new WeakHashMap<View, int[]>();
					mLayoutMap.put(header,
							new int[] { header.getLeft(), header.getTop(), header.getRight(), header.getBottom() });
				}
			}
		}
		updateHeaders();

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		post(new Runnable() {

			@Override
			public void run() {
				if (null == mScrollSrc) {
					oldt = t;
				} else {
					t = oldt = mScrollSrc.getScrollY();
				}
				updateHeaders();
			}
		});
	}

	@Override
	public void onScroll(int l, int t, int oldl, int oldt, int width, int height) {
		mHandler.removeCallbacks(mAggressivePurger);
		mHandler.postDelayed(mAggressivePurger, AGGRESSIVE_DELAY);
		switch (mWatchMode) {
		case Aggressive:
			mHandler.removeCallbacks(mAggressiveWatcher);
			mHandler.post(mAggressiveWatcher);
			mAggressiveWatchStarted = true;
		case Passive:
			passiveScroll(l, t, oldl, oldt, width, height);
			break;
		}

	}

	protected void passiveScroll(int l, int t, int oldl, int oldt, int width, int height) {
		this.oldl = this.l;
		this.oldt = this.t;
		this.l = l;
		this.t = t;
		this.width = width;
		this.height = height;
		updateHeaders();
	}

	private void updateHeaders() {
		if (null != mHeaders) {
			int top = getHeight();
			boolean found = false;
			for (int i = mHeaders.size() - 1; i >= 0; i--) {
				View child = mHeaders.get(i).get();
				if (null == child)
					continue;
				int[] orig = mLayoutMap.get(child);
				int childTop = orig[1] - t;
				if (found) {
					child.layout(orig[0], orig[1], orig[2], orig[3]);
					continue;
				}
				if (childTop <= 0) {
					found = true;
					int layoutTop = Math.max(Math.min(Math.min(t, top - child.getHeight()), getHeight() - height),
							orig[1]);
					child.bringToFront();
					child.layout(0, layoutTop, child.getWidth(), layoutTop + child.getHeight());
				} else {
					child.layout(orig[0], orig[1], orig[2], orig[3]);
					top = orig[1];
				}
			}
		}
	}

}
