package com.mmscn.titlebar;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.mmscn.widgets.R;

public class DropDownTitleBar extends NotificationTitleBar {
	public final static int STATE_TOP = 0;
	public final static int STATE_BOTTOM = 1;
	protected final static int STATE_TRANS_TO_TOP = 2;
	protected final static int STATE_TRANS_TO_BOTTOM = 3;
	private final static int DROP_DOWN_STEP_MAX = 255;
	private int mDropDownStep;
	private View mDropDownView;
	private int mDropDownBottom;
	private Scroller mScroller;
	private float mDropDownMarginHoriz;
	private float mDropDownMarginBottom;
	private int mDropDownPanelWidth;
	private int mDropDownPanelHeight;
	private int mDropDownPanelLeft;
	private int mState;
	private OnDropDownPanelStateChangedListener mListener;

	public DropDownTitleBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		Resources res = context.getResources();
		mDropDownMarginBottom = res.getDimension(R.dimen.title_bar_drop_down_panel_margin_bottom);
		mDropDownMarginHoriz = res.getDimension(R.dimen.title_bar_drop_down_panel_margin_horiz);
		mScroller = new Scroller(context);
	}

	public void bindDropDownView(View dropDownView) {
		this.mDropDownView = dropDownView;
	}

	public void bindDropDownView(int id) {
		this.mDropDownView = findViewById(id);
	}

	public void setOnDropDownPanelStateChangedListener(OnDropDownPanelStateChangedListener l) {
		this.mListener = l;
	}

	// @Override
	// public void onLayout(boolean changed, int left, int top, int right,
	// int bottom) {
	// if (mState == STATE_TOP || mState == STATE_TRANS_TO_TOP)
	// super.onLayout(changed, left, mDropDownBottom, right, bottom);
	// if (mDropDownView != null)
	// mDropDownView.layout(mDropDownPanelLeft, mDropDownBottom
	// - mDropDownPanelHeight, mDropDownPanelLeft
	// + mDropDownPanelWidth, mDropDownBottom);
	// }

	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			mDropDownStep = mScroller.getCurrY();
			updateDropDownPosition();
			requestLayout();
			invalidate();
		} else if (mState == STATE_TRANS_TO_BOTTOM) {
			mState = STATE_BOTTOM;
			mDropDownBottom = (int) (getTitleBarBottom() + mDropDownPanelHeight);
			provokeStateListener();
			requestLayout();
		} else if (mState == STATE_TRANS_TO_TOP) {
			mState = STATE_TOP;
			mDropDownBottom = (int) getTitleBarBottom();
			provokeStateListener();
			requestLayout();
			if (mDropDownView != null) {
				mDropDownView.setVisibility(View.GONE);
			}
		}
	}

	private void updateDropDownPosition() {
		mDropDownBottom = (int) ((float) mDropDownStep / DROP_DOWN_STEP_MAX * mDropDownPanelHeight + getPaddingTop());
	}

	private void provokeStateListener() {
		if (mListener != null) {
			mListener.onDropDownPanelStateChanged(mState);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mState == STATE_BOTTOM || mState == STATE_TRANS_TO_BOTTOM) {
				if (!isInDropDownPanel(ev.getX(), ev.getY())) {
					scrollDropDownPanelToTop();
					return true;
				}
			}
		}
		// return shouldInterceptTouchEvent() || super.dispatchTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}

	private boolean shouldInterceptTouchEvent() {
		return mState == STATE_TRANS_TO_BOTTOM || mState == STATE_TRANS_TO_TOP;
	}

	private boolean isInDropDownPanel(float x, float y) {
		return x > mDropDownPanelLeft && x < mDropDownPanelLeft + mDropDownPanelWidth
				&& y > mDropDownBottom - mDropDownPanelHeight && y < mDropDownBottom;
	}

	public boolean scrollDropDownPanelToBottom() {
		if (mState == STATE_TOP || mState == STATE_TRANS_TO_TOP) {
			if (mDropDownView != null) {
				mDropDownView.setVisibility(View.VISIBLE);
				mDropDownView.bringToFront();
			}
			mState = STATE_TRANS_TO_BOTTOM;
			scrollDropDownPanelTo((int) (getTitleBarBottom() + mDropDownPanelHeight));
			return true;
		}
		return false;
	}

	public void scrollDropDownPanelTo(int y) {
		if (mDropDownView == null) {
			return;
		}
		int step = (y - getPaddingTop()) / mDropDownPanelHeight * DROP_DOWN_STEP_MAX;
		mScroller.abortAnimation();
		mScroller.startScroll(0, mDropDownStep, 0, step - mDropDownStep, 400);
		invalidate();

	}

	// @Override
	// public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	// this.mDropDownPanelLeft = (int)
	// (Larder.TITLE_BAR_DROP_DOWN_PANEL_FULL_SCREEN ? 0
	// : mDropDownMarginHoriz);
	// this.mDropDownPanelHeight = (int)
	// (Larder.TITLE_BAR_DROP_DOWN_PANEL_FULL_SCREEN ? getActualHeight()
	// : getActualHeight() - mDropDownMarginBottom);
	// this.mDropDownPanelWidth = mWidth - 2 * mDropDownPanelLeft;
	// updateDropDownPosition();
	// if (mDropDownView != null) {
	// mDropDownView
	// .measure(MeasureSpec.makeMeasureSpec(mDropDownPanelWidth,
	// MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
	// mDropDownPanelHeight, MeasureSpec.EXACTLY));
	// }
	// }

	public boolean scrollDropDownPanelToTop() {
		if (mState == STATE_BOTTOM || mState == STATE_TRANS_TO_BOTTOM) {
			mState = STATE_TRANS_TO_TOP;
			scrollDropDownPanelTo((int) getTitleBarBottom());
			return true;
		}
		return false;
	}

	public boolean toggleDropDownPanel() {
		return scrollDropDownPanelToTop() || scrollDropDownPanelToBottom();
	}

	protected int getDropDownState() {
		return mState;
	}

	@Override
	public void onCenterRegionClicked() {
		super.onCenterRegionClicked();
		toggleDropDownPanel();
	}

	public interface OnDropDownPanelStateChangedListener {
		public void onDropDownPanelStateChanged(int state);
	}

}
