package com.mmscn.titlebar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Window;
import android.widget.Scroller;

import com.mmscn.widgets.R;

public class ProgressTitleBar extends TitleBarWithSubMenuButton {
	public final static int PROGRESS_END = Window.PROGRESS_END;
	public final static int PROGRESS_START = Window.PROGRESS_START;
	public final static int PROGRESS_STYLE_ABOVE_TITLE_BAR = 0;
	public final static int PROGRESS_STYLE_BELOW_TITLE_BAR = 1;
	public final static int PROGRESS_STYLE_BACKGROUND = 2;
	private Scroller mProgressScroller;
	/**
	 * used with PROGRESS_STYLE_ABOVE_TITLE_BAR and
	 * PROGRESS_STYLE_BELOW_TITLE_BAR
	 */
	private int mProgressBarWidth;
	private int mProgressStyle = 0;
	private Drawable mDrawableProgress;
	private int mProgress = PROGRESS_START;

	public ProgressTitleBar(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ProgressTitleBar);
		mProgressStyle = ta.getInt(R.styleable.ProgressTitleBar_progressbarStyle, PROGRESS_STYLE_BELOW_TITLE_BAR);
		int drawableId = ta.getResourceId(R.styleable.ProgressTitleBar_progressbarDrawable,
				R.color.title_bar_default_progress);
		mDrawableProgress = context.getResources().getDrawable(drawableId);
		mProgressBarWidth = ta.getDimensionPixelSize(R.styleable.ProgressTitleBar_progressbarWidth, 5);
		ta.recycle();
		mProgressScroller = new Scroller(context);
	}

	private boolean shouldDrawProgress() {
		return mProgress > PROGRESS_START && mProgress < PROGRESS_END;
	}

	public void setProgressBarStyle(int style) {
		this.mProgressStyle = style;
		if (shouldDrawProgress()) {
			updateProgress();
			invalidate();
		}
	}

	public void setProgress(int progress) {
		if (progress > mProgress) {
			mProgressScroller.abortAnimation();
			mProgressScroller.startScroll(mProgress, 0, progress - mProgress, 0, 500);
			invalidate();
		} else {
			forceProgress(progress);
		}
	}

	private void forceProgress(int progress) {
		this.mProgress = progress;
		if (shouldDrawProgress()) {
			updateProgress();
		}
		invalidate();
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mProgressScroller.computeScrollOffset()) {
			forceProgress(mProgressScroller.getCurrX());
		}
	}

	private void updateProgress() {
		Rect paddings = new Rect();
		if (mDrawableProgress.getPadding(paddings)) {
			switch (mProgressStyle) {
			case PROGRESS_STYLE_BELOW_TITLE_BAR:
				mDrawableProgress.setBounds(-paddings.left, (int) mTitleBarHeight - mProgressBarWidth - paddings.top,
						mWidth * (mProgress - PROGRESS_START) / (PROGRESS_END - PROGRESS_START) + paddings.right,
						(int) mTitleBarHeight + paddings.bottom);
				break;
			case PROGRESS_STYLE_ABOVE_TITLE_BAR:
				mDrawableProgress.setBounds(-paddings.left, -paddings.top, mWidth * (mProgress - PROGRESS_START)
						/ (PROGRESS_END - PROGRESS_START) + paddings.right, mProgressBarWidth + paddings.bottom);
				break;
			case PROGRESS_STYLE_BACKGROUND:
				mDrawableProgress.setBounds(-paddings.left, -paddings.top, mWidth * (mProgress - PROGRESS_START)
						/ (PROGRESS_END - PROGRESS_START) + paddings.right, (int) mTitleBarHeight + paddings.bottom);
				break;
			}
		} else {
			switch (mProgressStyle) {
			case PROGRESS_STYLE_BELOW_TITLE_BAR:
				mDrawableProgress.setBounds(0, (int) mTitleBarHeight - mProgressBarWidth, mWidth
						* (mProgress - PROGRESS_START) / (PROGRESS_END - PROGRESS_START), (int) mTitleBarHeight);
				break;
			case PROGRESS_STYLE_ABOVE_TITLE_BAR:
				mDrawableProgress.setBounds(0, 0, mWidth * (mProgress - PROGRESS_START)
						/ (PROGRESS_END - PROGRESS_START), mProgressBarWidth);
				break;
			case PROGRESS_STYLE_BACKGROUND:
				mDrawableProgress.setBounds(0, 0, mWidth * (mProgress - PROGRESS_START)
						/ (PROGRESS_END - PROGRESS_START), (int) mTitleBarHeight);
				break;
			}
		}
	}

	@Override
	protected void drawTitleBarBg(Canvas canvas) {
		super.drawTitleBarBg(canvas);
		if (shouldDrawProgress())
			mDrawableProgress.draw(canvas);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		updateProgress();
	}

}
