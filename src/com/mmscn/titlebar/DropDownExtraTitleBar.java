package com.mmscn.titlebar;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.mmscn.widgets.R;

public class DropDownExtraTitleBar extends DropDownTitleBar {
	public final static int EXTRA_TOP = 0;
	public final static int EXTRA_BOTTOM = 1;
	protected final static int EXTRA_TRANS_TO_TOP = 2;
	protected final static int EXTRA_TRANS_TO_BOTTOM = 3;
	private View mExtraView;
	private int mExtraPanelLeft;
	private float mExtraPanelWidth;
	private float mExtraPanelHeight;
	private float mExtraPanelBottom;
	private int mState;
	private Scroller mScroller;

	public DropDownExtraTitleBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public void bindExtraView(View v) {
		this.mExtraView = v;
	}

	public void bindExtraView(int id) {
		this.mExtraView = findViewById(id);
	}

	private void init(Context context) {
		Resources res = context.getResources();
		mExtraPanelWidth = res.getDimension(R.dimen.title_bar_extra_panel_width);
		mExtraPanelHeight = res.getDimension(R.dimen.title_bar_extra_panel_height);

		mScroller = new Scroller(context);
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			mExtraPanelBottom = mScroller.getCurrY();
			requestLayout();
			invalidate();
		} else if (mState == EXTRA_TRANS_TO_BOTTOM) {
			mState = STATE_BOTTOM;
			mExtraPanelBottom = getTitleBarBottom() + mExtraPanelHeight;
			requestLayout();
		} else if (mState == EXTRA_TRANS_TO_TOP) {
			mState = STATE_TOP;
			mExtraPanelBottom = (int) getTitleBarBottom();
			requestLayout();
			if (null != mExtraView) {
				mExtraView.setVisibility(View.GONE);
			}
		}
	}

	public boolean scrollExtraPanelToTop() {
		if (mState == EXTRA_BOTTOM || mState == EXTRA_TRANS_TO_BOTTOM) {
			mState = EXTRA_TRANS_TO_TOP;
			scrollExtraPanelTo((int) getTitleBarBottom());
			return true;
		}
		return false;
	}

	protected int getExtraState() {
		return mState;
	}

	public boolean scrollExtraPanelToBottom() {
		if (mState == EXTRA_TOP || mState == EXTRA_TRANS_TO_TOP) {
			if (mExtraView != null) {
				mExtraView.setVisibility(View.VISIBLE);
				mExtraView.bringToFront();
			}
			mState = EXTRA_TRANS_TO_BOTTOM;
			scrollExtraPanelTo((int) (getTitleBarBottom() + mExtraPanelHeight));
			return true;
		}
		return false;
	}

	public boolean toggleExtraPanel() {
		if (getDropDownState() != STATE_TOP) {
			scrollDropDownPanelToTop();
		}
		return scrollExtraPanelToTop() || scrollExtraPanelToBottom();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mState == EXTRA_BOTTOM && isInExtraPanel(ev.getX(), ev.getY())) {
				scrollExtraPanelToTop();
				return true;
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	private boolean isInExtraPanel(float x, float y) {
		return x > mExtraPanelLeft && x < mExtraPanelLeft + mExtraPanelWidth && y > getTitleBarBottom()
				&& y < getTitleBarBottom() + mExtraPanelHeight;
	}

	private void scrollExtraPanelTo(int y) {
		if (mExtraView == null) {
			return;
		}
		mScroller.abortAnimation();
		mScroller.startScroll(0, (int) mExtraPanelBottom, 0, (int) (y - mExtraPanelBottom));
		invalidate();
	}

	@Override
	public boolean scrollDropDownPanelToBottom() {
		scrollExtraPanelToTop();
		return super.scrollDropDownPanelToBottom();
	}

	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mExtraView != null) {
			mExtraView.layout(mExtraPanelLeft, (int) (mExtraPanelBottom - mExtraPanelHeight), mWidth,
					(int) (mExtraPanelBottom));
		}
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mExtraView != null) {
			mExtraView.measure(MeasureSpec.makeMeasureSpec((int) mExtraPanelWidth, MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec((int) mExtraPanelHeight, MeasureSpec.EXACTLY));
		}
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.mExtraPanelLeft = (int) (w - mExtraPanelWidth);
	}

}
