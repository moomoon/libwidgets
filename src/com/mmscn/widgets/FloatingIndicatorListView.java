package com.mmscn.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;

public class FloatingIndicatorListView extends ListView implements
		OnScrollListener {
	public final static String CLEAR_INDICATOR = "//shut//up//";

	private final static int INDICATOR_LIFE = 1000;
	private Handler mHandler;
	private String mIndicator;
	private boolean shouldDrawIndicator;
	protected int mHeight, mWidth;
	private float mIndicatorHeight;
	private float mIndicatorWidth;
	private float MIN_INDICATOR_WIDTH;
	private float PADDING;
	private Paint mPt;
	private int mColorIndicatorText;
	private int mColorIndicatorBg;
	private int mIndicatorTextHeight;
	private OnScrollListener mListener;
	private int mScrollState;
	private int mLastIndicatorSource = -1;
	private boolean shouldWatchFling;
	private Runnable mIndicatorRemover = new Runnable() {
		@Override
		public void run() {
			shouldDrawIndicator = false;
			invalidate();
		};
	};

	public FloatingIndicatorListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		
		
		
		
		
		
		Resources res = context.getResources();
		mColorIndicatorBg = res.getColor(R.color.floating_indicator_bg);
		mColorIndicatorText = res.getColor(R.color.floating_indicator_text);
		mIndicatorHeight = res
				.getDimension(R.dimen.floating_indicator_bg_height);
		float textSize = res.getDimension(R.dimen.floating_indicator_text);
		PADDING = res
				.getDimension(R.dimen.floating_indicator_padding_horizontal);
		MIN_INDICATOR_WIDTH = res
				.getDimension(R.dimen.floating_indicator_bg_min_width);

		mPt = new Paint();
		mPt.setTextSize(textSize);
		mPt.setTextAlign(Align.CENTER);
		mPt.setAntiAlias(true);

		mHandler = new Handler();
		super.setOnScrollListener(this);
		
		
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		clearIndicator();
		super.setAdapter(adapter);
	}

	public void clearIndicator() {
		shouldDrawIndicator = false;
		mHandler.removeCallbacks(mIndicatorRemover);
		mIndicator = null;
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		this.mListener = l;
	}

	public interface FloatingIndicatorCreator {
		public String createIndicatorCreator();

		public boolean shouldShowIndicatorWhenFling();
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (mListener != null) {
			mListener.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount);
		}
		if (visibleItemCount <= 0) {
			return;
		}
		Object obj;
		if ((obj = getChildAt(0).getTag()) == null
				|| !(obj instanceof FloatingIndicatorCreator)) {
			return;
		}
		shouldWatchFling = ((FloatingIndicatorCreator) obj)
				.shouldShowIndicatorWhenFling();
		String s = ((FloatingIndicatorCreator) obj).createIndicatorCreator();
		if (s == null) {
			return;
		}
		if ((this.mIndicator == null || !mIndicator.matches(s))
				&& mLastIndicatorSource != firstVisibleItem) {
			if (s.matches(CLEAR_INDICATOR)) {
				clearIndicator();
				return;
			}
			mHandler.removeCallbacks(mIndicatorRemover);
			shouldDrawIndicator = true;
			mIndicator = s;
			Rect r = new Rect();
			mPt.getTextBounds(s, 0, s.length(), r);
			mIndicatorTextHeight = r.height();
			mIndicatorWidth = Math.max(MIN_INDICATOR_WIDTH,
					2 * PADDING + r.width());
			mHandler.postDelayed(mIndicatorRemover, INDICATOR_LIFE);
			mLastIndicatorSource = firstVisibleItem;
			invalidate();
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if ((!shouldDrawIndicator && !(mScrollState == SCROLL_STATE_FLING && shouldWatchFling))
				|| mIndicator == null) {
			return;
		}
		mPt.setColor(mColorIndicatorBg);
		canvas.drawRect((mWidth - mIndicatorWidth) / 2,
				(mHeight - mIndicatorHeight) / 2,
				(mWidth + mIndicatorWidth) / 2,
				(mHeight + mIndicatorHeight) / 2, mPt);
		mPt.setColor(mColorIndicatorText);
		canvas.drawText(mIndicator, mWidth / 2,
				(mHeight + mIndicatorTextHeight) / 2, mPt);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mListener != null) {
			mListener.onScrollStateChanged(view, scrollState);
		}
		this.mScrollState = scrollState;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		this.mHeight = MeasureSpec.getSize(heightMeasureSpec);
		this.mWidth = MeasureSpec.getSize(widthMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
