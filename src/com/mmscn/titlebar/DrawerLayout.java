package com.mmscn.titlebar;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.mmscn.widgets.R;

//抽屉布局
public class DrawerLayout extends FrameLayout {
	private final static int TOTAL_STEPS = 1000;
	private int mStep;
	private float mStepWidth;

	private final static float SLIDE_TIME_MULTIPLIER = 0.5f;

	private int topId, bottomId;

	// 抽屉顶端页
	private View topView = null;

	// 抽屉底部页
	private View bottomView = null;

	// 抽屉状态
	private int mState;

	// 伪抽屉状态，仅有顶/底两种
	private int mFakeState;

	// 抽屉是否运动
	private boolean isMoving;

	public static final int STATE_TOP = 0;
	public static final int STATE_BOTTOM = 1;
	public static final int STATE_TRANS_TO_TOP = 2;
	public static final int STATE_TRANS_TO_BOTTOM = 3;
	public static final int STATE_DRAG = 4;

	// 松手后翻到顶部left门限
	private float mSwitchToThresholdTop;

	// 松手后翻到底部left门限
	private float mSwitchToThresholdBottom;

	// 顶部view left值
	// private int mTopLeft;

	// 横向运动开始门限
	private static final int mHorizontalMoveThreshold = 5;

	// 纵向运动允许值
	private static final int mVerticalMoveSlop = 20;

	// view宽度
	private int mWidth, mHeight;

	// 抽屉右侧留边宽度
	private int mEdgeWidth;

	private Scroller mScroller;
	private Context mContext;
	private GestureDetector mDragDetector;
	private GestureDetector mClickDetector;

	// 从顶部开启拖动的区域
	private int mTriggerThreshold;
	private int mTriggerMode;

	private boolean isDragDisabled = true;
	private boolean isClickDisabled = false;

	public final static int TRIGGER_EVERYWHERE = 0;
	public final static int TRIGGER_TO_LEFT_OF = 1;
	public final static int TRIGGER_TO_RIGHT_OF = 2;

	private DrawerActionListener mListener;

	public DrawerLayout(Context context) {
		this(context, null);
	}

	public DrawerLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init(context, attrs);
	}

	public void setDrawerActionListener(DrawerActionListener l) {
		this.mListener = l;

	}

	public void setIsDragDisabled(boolean isDragDisabled) {
		this.isDragDisabled = isDragDisabled;
	}

	public void setIsClickDisabled(boolean isClickDisabled) {
		this.isClickDisabled = isClickDisabled;
	}

	public void setTriggerArea(int triggerMode, int threshold) {
		// 设定触发区域
		this.mTriggerMode = triggerMode;
		this.mTriggerThreshold = threshold;
	}

	public final int getState() {
		return mState;
	}

	private void init(Context context, AttributeSet attrs) {
		// mTopLeft = 0;
		mStep = 0;
		mEdgeWidth = (int) context.getResources().getDimension(R.dimen.drawer_edge_width);
		mFakeState = STATE_TOP;
		mState = STATE_TOP;
		mScroller = new Scroller(mContext);
		mDragDetector = new GestureDetector(mContext, new DragListener());
		mClickDetector = new GestureDetector(mContext, new ClickListener());
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DrawableLayout);
		topId = a.getResourceId(R.styleable.DrawableLayout_top, 0);
		bottomId = a.getResourceId(R.styleable.DrawableLayout_bottom, 0);
		a.recycle();

	}

	public void setState(int state) {
		this.mState = state;
	}

	public void bindViews(int topId, int bottomId) {
		bindViews(findViewById(topId), findViewById(bottomId));
	}

	public void bindViews(View top, View bottom) {
		// 绑定顶端/底端view
		this.bottomView = bottom;
		this.topView = top;

		// updateBottomViewVisibility();
		requestLayout();
		if (topView != null) {
			// topView.setDrawingCacheEnabled(true);
			topView.requestLayout();
			topView.invalidate();
		}
		if (bottomView != null) {
			if (mState == STATE_TOP) {
				bottomView.setVisibility(View.GONE);
			} else {
				bottomView.requestLayout();
				bottomView.invalidate();
				bottomView.setVisibility(View.VISIBLE);
			}
		}
		invalidate();
	}

	private void startDrag() {
		// 进入拖动状态
		mState = STATE_DRAG;
		isMoving = true;
		if (this.mListener != null) {
			this.mListener.onStartDrag();
		}
		if (bottomView != null) {
			bottomView.setVisibility(View.VISIBLE);
		}
		// updateBottomViewVisibility();
		invalidate();
	}

	private void drag(float dx) {
		// 拖动dx距离
		mStep -= dx / mStepWidth;
		mStep = Math.max(0, Math.min(mStep, TOTAL_STEPS));
		// mShadowLeft = mScrollX - mShadow.getWidth() + 1;
		if (topView != null) {
			topView.layout(getTopLeft(), 0, mWidth + getTopLeft(), getHeight());
		}
		invalidate();
	}

	private int getTopLeft() {
		return (int) (mStep * mStepWidth);
	}

	public void smoothScrollToTop() {
		// 滑动到顶部
		if (mStep <= 0) {
			mStep = 0;

			isMoving = false;
			mState = STATE_TOP;
			mFakeState = STATE_TOP;
			requestLayout();
			invalidate();

			return;
		}

		mState = STATE_TRANS_TO_TOP;
		isMoving = true;
		mScroller.abortAnimation();
		int t = (int) (Math.abs(getTopLeft()) * SLIDE_TIME_MULTIPLIER);
		mScroller.startScroll((int) mStep, 0, (int) (-mStep), 0, t);
		// updateBottomViewVisibility();
		requestLayout();
		invalidate();
	}

	public void smoothScrollToBottom() {
		// 滑动到底部
		mState = STATE_TRANS_TO_BOTTOM;
		isMoving = true;
		mScroller.abortAnimation();
		int t = (int) (Math.abs(mWidth - mEdgeWidth - getTopLeft()) * SLIDE_TIME_MULTIPLIER);
		mScroller.startScroll((int) mStep, 0, (int) (TOTAL_STEPS - mStep), 0, t);

		if (bottomView != null) {
			bottomView.setVisibility(View.VISIBLE);
		}
		requestLayout();
		invalidate();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean intercepted = false;
		if (!isClickDisabled)
			intercepted |= mClickDetector.onTouchEvent(ev);
		if (!intercepted && !isDragDisabled)
			intercepted |= mDragDetector.onTouchEvent(ev) || processTouch(ev) || isMoving || dispatchMotionEvent(ev);
		if (!intercepted)
			intercepted |= super.onInterceptTouchEvent(ev);
		return intercepted;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void computeScroll() {
		// boolean t;
		if (mScroller.computeScrollOffset()) {
			mStep = mScroller.getCurrX();
			if (topView != null) {
				topView.layout(getTopLeft(), 0, mWidth + getTopLeft(), getHeight());
			}
			// requestLayout();
			invalidate();
			// computeScroll();
			// requestLayout();
		} else if (mState == STATE_TRANS_TO_TOP) {
			mStep = 0;

			mFakeState = STATE_TOP;
			mState = STATE_TOP;
			isMoving = false;
			if (this.mListener != null) {
				this.mListener.onStopDragAt(mState);
			}
			if (bottomView != null) {
				bottomView.setVisibility(View.GONE);
			}
			// invalidate();
			// updateBottomViewVisibility();
			requestLayout();
		} else if (mState == STATE_TRANS_TO_BOTTOM) {
			mStep = TOTAL_STEPS;

			mFakeState = STATE_BOTTOM;
			mState = STATE_BOTTOM;
			isMoving = false;
			if (this.mListener != null) {
				this.mListener.onStopDragAt(mState);
			}
			// invalidate();
			requestLayout();
		}
	}

	// private void updateBottomViewVisibility() {
	// if (bottomView != null) {
	// bottomView.setVisibility(mState == STATE_BOTTOM ? View.GONE
	// : View.VISIBLE);
	// }
	// }

	private boolean processTouch(MotionEvent ev) {
		// 处理up事件
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:

			if (mState == STATE_DRAG) {
				if (getTopLeft() > (mFakeState == STATE_TOP ? mSwitchToThresholdTop : mSwitchToThresholdBottom)) {
					smoothScrollToBottom();
				} else {
					smoothScrollToTop();
				}
				return true;
			}

		}
		return false;
	}

	private boolean dispatchMotionEvent(MotionEvent e) {
		return e.getX() > getTopLeft() ? mState == STATE_TOP ? topView.dispatchTouchEvent(e) : true : bottomView
				.dispatchTouchEvent(e);
	}

	// @Override
	// public void onSizeChanged(int w, int h, int oldw, int oldh) {
	// this.mWidth = w;
	// this.mHeight = h;
	// mSwitchToThresholdTop = w / 10;
	// mSwitchToThresholdBottom = w * 3 / 4;
	// if (h > 0 && w > 0)
	// mShadow = Bitmap.createScaledBitmap(shadow, shadow.getWidth(), h,
	// false);
	// super.onSizeChanged(w, h, oldw, oldh);
	// }
	private boolean shouldTrigger(float x) {
		return mTriggerMode == TRIGGER_EVERYWHERE || (mTriggerMode == TRIGGER_TO_LEFT_OF && x <= mTriggerThreshold)
				|| (mTriggerMode == TRIGGER_TO_RIGHT_OF && x >= mTriggerThreshold);
	}

	private class ClickListener extends SimpleOnGestureListener {

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// 若点击右侧留边区域，则滑动回顶部

			if (mState == STATE_BOTTOM && e.getX() >= getTopLeft()) {
				smoothScrollToTop();
				return true;
			}
			return false;
		}
	}

	private class DragListener extends SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if ((!isMoving) && Math.abs(e2.getY() - e1.getY()) > mVerticalMoveSlop || (!shouldTrigger(e1.getX()))) {
				return false;
			}
			if (mState == STATE_TOP) {
				if (e2.getX() - e1.getX() > mHorizontalMoveThreshold) {
					if (shouldTrigger(e1.getX())) {
						// 若处于触发区域内，则开始拖动
						startDrag();
					}
				}
			} else if (mState == STATE_BOTTOM && e1.getX() > mWidth - mEdgeWidth
					&& e1.getX() - e2.getX() > mHorizontalMoveThreshold) {
				// 若处于右侧留边区域内，则开始拖动
				startDrag();
			} else if (mState == STATE_DRAG) {
				drag(distanceX);
			}
			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			if (mState == STATE_BOTTOM && e.getX() >= getTopLeft()) {
				smoothScrollToTop();
				return true;
			}
			return false;
		}

	}

	public void toggleDrawer() {

		switch (mState) {
		case STATE_BOTTOM:
		case STATE_TRANS_TO_BOTTOM:
			smoothScrollToTop();
			break;
		case STATE_TOP:
		case STATE_TRANS_TO_TOP:
			smoothScrollToBottom();
			break;
		}
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mWidth = MeasureSpec.getSize(widthMeasureSpec);
		mHeight = MeasureSpec.getSize(heightMeasureSpec);

		mStepWidth = (float) mEdgeWidth / TOTAL_STEPS;

		mSwitchToThresholdTop = mWidth / 10;
		mSwitchToThresholdBottom = mWidth * 3 / 4;
		// if (mHeight > 0 && mWidth > 0)
		// mShadow = Bitmap.createScaledBitmap(shadow, shadow.getWidth(),
		// mHeight,
		// false);
		// if (topView != null) {
		// topView.measure(MeasureSpec.makeMeasureSpec(mWidth,
		// MeasureSpec.EXACTLY),
		// MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY));
		// topView.invalidate();
		// }
		// if (bottomView != null) {
		// bottomView.measure(MeasureSpec.makeMeasureSpec(mWidth,
		// MeasureSpec.EXACTLY),
		// MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY));
		// bottomView.invalidate();
		// }
		// requestLayout();

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// Notification.i("on layout");
		// super.onLayout(changed, l, t, r, b);
		super.onLayout(changed, l, t, r, b);
		if (null == topView || null == bottomView) {
			bindViews(topId, bottomId);
		}
		if (topView != null) {
			topView.layout(getTopLeft(), 0, mWidth + getTopLeft(), mHeight + getTopLeft());
		}
		// if (bottomView != null) {
		// bottomView.layout(0, 0, mWidth, mHeight);
		// }
	}

	public interface DrawerActionListener {

		public void onStartDrag();

		public void onStopDragAt(int state);

	}

}
