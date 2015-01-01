package com.mmscn.tabholder;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Scroller;

import com.mmscn.titlebar.DropDownExtraTitleBar;
import com.mmscn.widgets.R;

public class TabHolder extends DropDownExtraTitleBar {
	private final static int SHADE_ALPHA_MAX = 128;
	private final static int SHADE_APLHA_MIN = 0;
	private final static int INDICATOR_DELAY = 400;

	private int mShadeAlpha;
	private Paint mPtIcon;
	private Paint mPtText;
	private Paint mPtTextSelected;
	private Paint mPtIndicator;
	private Paint mPtExtraPanelBg;
	private Paint mPtTabHolderBg;
	private Paint mPtLine;

	private int mNumSqTotal = 4;
	private int mNumTabs;

	private static int mWidth, mHeight;
	private Scroller mIndicatorScroller;
	private Scroller mShadeScroller;
	private float mIndicatorLeft;
	private float mIndicatorHeight;
	private float mExtraLineWidth;
	private float mExtraLineMargin;
	private float mExtraTabHeight;
	private int mNumExtraColumns = 4;
	private float mNumExtraTabs;
	private float mNumExtraRows;
	private float mExtraPanelHeight = 100;
	private float mExtraTabWidth;
	private float mTabHolderHeight;
	private float mExtraPanelTop;
	private List<Tab> mTabList;
	private List<Tab> mExtraTabList;
	private Context mContext;
	private OnTabSelectedListener mListener;
	private float mSqWidth;
	private int i;
	private float left, top;
	private boolean isSelected;
	private Tab tab;
	private Bitmap bmpDummy;
	private Bitmap extraTabButton;
	private Bitmap extraTabButtonSelected;
	private float mExtraBtWidth;
	private int mSelectedPosition = -1;
	private int mSelectedExtraPosition = -1;

	private float mTitlePaddingBottom;
	private float mIconPaddingBottom;

	private boolean shouldShowExtraPanel;
	private GestureDetector mDetector;

	private int mColorExtraPanelShade;
	private boolean mScrollChanged;

	private Handler mHandler;

	public TabHolder(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), (int) (getPaddingBottom() + mTabHolderHeight));
		setClickable(true);
	}

	private void init(Context context) {
		this.mContext = context;

		Resources res = context.getResources();

		int colorText = res.getColor(R.color.tab_holder_text);
		int colorTextSelected = res.getColor(R.color.tab_holder_text_selected);
		int colorIndicator = res.getColor(R.color.tab_holder_indicator);
		int colorExtraPanelBg = res.getColor(R.color.tab_holder_extra_panel_bg);
		int colorLine = res.getColor(R.color.tab_holder_extra_panel_line);
		mColorExtraPanelShade = res.getColor(R.color.tab_holder_extra_panel_shade);

		float textSize = res.getDimension(R.dimen.tab_holder_text);
		mTabHolderHeight = res.getDimension(R.dimen.tab_holder_height_portrait);
		mTitlePaddingBottom = res.getDimension(R.dimen.tab_holder_title_padding_bottom);
		mIconPaddingBottom = res.getDimension(R.dimen.tab_holder_icon_padding_bottom);
		mIndicatorHeight = res.getDimension(R.dimen.tab_holder_indicator_height);
		mExtraBtWidth = res.getDimension(R.dimen.tab_holder_extra_tab_button_width);
		mExtraTabHeight = res.getDimension(R.dimen.tab_holder_extra_tab_height);
		mExtraLineMargin = res.getDimension(R.dimen.tab_holder_extra_panel_line_margin);
		mExtraLineWidth = res.getDimension(R.dimen.tab_holder_extra_panel_line_width);

		extraTabButton = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
		extraTabButtonSelected = extraTabButton;

		mPtIcon = new Paint();
		mPtIndicator = new Paint();
		mPtText = new Paint();
		mPtTextSelected = new Paint();
		mPtExtraPanelBg = new Paint();
		mPtTabHolderBg = new Paint();
		mPtLine = new Paint();

		mPtIndicator.setColor(colorIndicator);
		mPtText.setColor(colorText);
		mPtTextSelected.setColor(colorTextSelected);
		mPtExtraPanelBg.setColor(colorExtraPanelBg);
		mPtTabHolderBg.setColor(colorExtraPanelBg);
		mPtLine.setColor(colorLine);

		mPtText.setTextSize(textSize);
		mPtTextSelected.setTextSize(textSize);

		mPtText.setTextAlign(Align.CENTER);
		mPtTextSelected.setTextAlign(Align.CENTER);

		mPtText.setAntiAlias(true);
		mPtTextSelected.setAntiAlias(true);

		mPtIndicator.setStrokeWidth(mIndicatorHeight);

		mShadeAlpha = SHADE_APLHA_MIN;
		applyShadeAlpha();

		mDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapUp(MotionEvent event) {

				if (event.getY() > mHeight - mTabHolderHeight) {
					if (event.getX() > mWidth - mExtraBtWidth) {
						toggleExtraTabPanel();
					} else {
						flipTo(pointToPosition(event.getX()), false);
					}

					return true;
				} else if (shouldShowExtraPanel) {
					fireExtraTab(pointToExtraPosition(event.getX(), event.getY()));

					return true;
				}
				return false;
			}

			@Override
			public boolean onDown(MotionEvent event) {
				if (shouldShowExtraPanel && !isInTabHolder(event.getY())) {
					setShouldShowExtraPanel(false);
					invalidate();
					return true;

				}
				return false;
			}
		});

		mIndicatorScroller = new Scroller(context);
		mShadeScroller = new Scroller(context);
		setClickable(true);

		mHandler = new Handler();
	}

	private void applyShadeAlpha() {
		mColorExtraPanelShade &= 0xffffff;
		mColorExtraPanelShade += mShadeAlpha << (4 * 6);
	}

	public void setOnTabSelectedListener(OnTabSelectedListener l) {
		this.mListener = l;
	}

	public Tab newTab() {
		if (mTabList == null) {
			mTabList = new ArrayList<Tab>();
		}
		Tab t = new Tab();
		t.id = mTabList.size();
		mTabList.add(t);
		mNumTabs = mTabList.size();
		return t;
	}

	public Tab newExtraTab() {
		if (mTabList == null) {
			mTabList = new ArrayList<Tab>();
		}
		if (mExtraTabList == null) {
			mExtraTabList = new ArrayList<Tab>();
		}
		Tab t = new Tab();
		t.id = mTabList.size() + mExtraTabList.size();
		mExtraTabList.add(t);
		mNumExtraTabs = mExtraTabList.size();
		updateExtraTabPosition();
		return t;
	}

	private void fireExtraTab(int id) {
		Log.i("flipto", "flipto " + id);
		if (id >= 0 && id != mSelectedExtraPosition) {
			// mScroller.abortAnimation();
			// mScroller.startScroll((int) mIndicatorLeft, 0,
			// (int) (getLeft(id) - mIndicatorLeft), 0);
			mSelectedExtraPosition = id;
			if (mListener != null && id < mNumTabs) {
				mListener.onTabSelected(id + mNumTabs);
			}
			setShouldShowExtraPanel(false);
			invalidate();

		}
	}

	public void toggleExtraTabPanel() {
		setShouldShowExtraPanel(!shouldShowExtraPanel);
		invalidate();
	}

	public void setShouldShowExtraPanel(boolean shouldShowExtraPanel) {
		if (shouldShowExtraPanel) {
			tryShrinkDropDownAndExtraPanels();
		}
		if (this.shouldShowExtraPanel != shouldShowExtraPanel) {
			mShadeScroller.abortAnimation();
			mShadeScroller.startScroll(mShadeAlpha, 0, (shouldShowExtraPanel ? SHADE_ALPHA_MAX : SHADE_APLHA_MIN)
					- mShadeAlpha, 0, 800);
			this.shouldShowExtraPanel = shouldShowExtraPanel;
		}
	}

	private void tryShrinkDropDownAndExtraPanels() {
		if (getExtraState() == EXTRA_BOTTOM || getExtraState() == EXTRA_TRANS_TO_BOTTOM) {
			scrollExtraPanelToTop();
		}
		if (getDropDownState() == STATE_BOTTOM || getDropDownState() == STATE_TRANS_TO_BOTTOM) {
			scrollDropDownPanelToTop();
		}
	}

	private int pointToExtraPosition(float x, float y) {
		for (i = 0; i < mNumExtraTabs; i++) {
			left = getExtraTabLeft(i);
			top = getExtraTabTop(i);
			if (x > left && x < left + mExtraTabWidth && y > top && y < top + mExtraTabHeight) {
				return i;
			}
		}
		return -1;
	}

	private void updateExtraTabPosition() {
		mNumExtraRows = (int) Math.ceil((double) mNumExtraTabs / mNumExtraColumns);
		mExtraPanelHeight = mNumExtraRows * mExtraTabHeight;
		mExtraPanelTop = mHeight - mTabHolderHeight - mExtraPanelHeight;
	}

	public void setNumExtraColumns(int numColumns) {
		this.mNumExtraColumns = numColumns;
		updateSqSize();
	}

	public class Tab {
		public Bitmap icon;
		public Bitmap iconSelected;
		public String title;
		private int id;

		public Tab setIcon(Bitmap icon) {
			this.icon = icon;
			if (iconSelected == null) {
				iconSelected = icon;
			}
			return this;
		}

		public Tab setIconSelected(Bitmap iconSelected) {
			this.iconSelected = iconSelected;
			return this;
		}

		public Tab setTitle(String title) {
			this.title = title;
			return this;
		}

		public Tab setTitle(int resId) {
			title = mContext.getResources().getString(resId);
			return this;
		}

		public int getItemId() {
			return id;
		}
	}

	public int pointToPosition(float x) {
		for (i = 0; i < mNumTabs; i++) {
			left = getLeft(i);
			if (x > left && x < left + mSqWidth) {
				return i;
			}

		}

		return -1;
	}

	private float getLeft(int id) {
		return mSqWidth * id;
	}

	private float getExtraTabLeft(int id) {
		return mExtraTabWidth * (id % mNumExtraColumns);
	}

	private float getExtraTabTop(int id) {
		return mExtraPanelTop + mExtraTabHeight * (id / mNumExtraColumns);
	}

	public void setNumSqTotal(int numSqTotal) {
		this.mNumSqTotal = numSqTotal;
		updateSqSize();
	}

	public void updateSqSize() {
		mSqWidth = (mWidth - mExtraBtWidth) / mNumSqTotal;
		mExtraTabWidth = mWidth / mNumExtraColumns;
	}

	public void clearTabs() {
		mTabList = null;
		mExtraTabList = null;
		mNumTabs = 0;
		mNumExtraTabs = 0;
	}

	@Override
	public void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		canvas.drawColor(mColorExtraPanelShade);

		// draw extra tab panel
		if (shouldShowExtraPanel) {
			canvas.drawRect(0, mExtraPanelTop, mWidth, mHeight - mTabHolderHeight, mPtExtraPanelBg);
			if (hasSelectedExtraTab()) {
				drawExtraTab(mTabList.get(mSelectedPosition), getExtraTabLeft(0), getExtraTabTop(0), canvas);
				for (i = 0; i < mNumExtraTabs; i++) {
					if (i < mSelectedExtraPosition) {
						drawExtraTab(mExtraTabList.get(i), getExtraTabLeft(i + 1), getExtraTabTop(i + 1), canvas);
					} else if (i > mSelectedExtraPosition) {

						drawExtraTab(mExtraTabList.get(i), getExtraTabLeft(i), getExtraTabTop(i), canvas);
					}
				}
			} else {
				for (i = 0; i < mNumExtraTabs; i++) {
					drawExtraTab(mExtraTabList.get(i), getExtraTabLeft(i), getExtraTabTop(i), canvas);
				}
			}
			mPtLine.setStrokeWidth(mExtraLineWidth);
			for (i = 1; i < mNumExtraRows; i++) {
				top = mExtraPanelTop + i * mExtraTabHeight;
				canvas.drawLine(mExtraLineMargin, top, mWidth - mExtraLineMargin, top, mPtLine);
			}
			for (i = 1; i < mNumExtraColumns; i++) {
				left = mExtraTabWidth * i;
				canvas.drawLine(left, mExtraPanelTop + mExtraLineMargin, left, mHeight - mTabHolderHeight
						- mExtraLineMargin, mPtLine);
			}
		}

		// draw main tab panel
		canvas.drawRect(0, mHeight - mTabHolderHeight, mWidth, mHeight, mPtTabHolderBg);
		if (mNumTabs > 0)
			for (i = 0; i < mNumTabs; i++) {
				drawTab(i, canvas);
			}
		bmpDummy = shouldShowExtraPanel ? extraTabButtonSelected : extraTabButton;
		if (bmpDummy != null) {
			canvas.drawBitmap(bmpDummy, getLeft(mNumTabs) + mExtraBtWidth / 2 - bmpDummy.getWidth() / 2, mHeight
					- mIconPaddingBottom - bmpDummy.getHeight(), mPtIcon);
		}
		drawIndicator(canvas);
	}

	private boolean hasSelectedExtraTab() {
		return mSelectedExtraPosition >= 0 && mSelectedExtraPosition < mNumExtraTabs;
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		mScrollChanged = false;
		if (mIndicatorScroller.computeScrollOffset()) {
			mIndicatorLeft = mIndicatorScroller.getCurrX();
			mScrollChanged = true;
		} else {
			mIndicatorLeft = getLeft(mSelectedPosition);
		}
		if (mShadeScroller.computeScrollOffset()) {
			mShadeAlpha = mShadeScroller.getCurrX();
			Log.i("calcalpha", "alpha = " + mShadeAlpha);
			applyShadeAlpha();
			mScrollChanged = true;
		}
		if (mScrollChanged) {
			invalidate();
		}
	}

	private void drawTab(int id, Canvas canvas) {
		left = getLeft(id);
		isSelected = id == mSelectedPosition;
		tab = mTabList.get(id);
		bmpDummy = isSelected ? tab.iconSelected : tab.icon;
		if (bmpDummy != null)
			canvas.drawBitmap(bmpDummy, left + mSqWidth / 2 - bmpDummy.getWidth() / 2, mHeight - mIconPaddingBottom
					- bmpDummy.getHeight(), mPtIcon);
		canvas.drawText(tab.title, left + mSqWidth / 2, mHeight - mTitlePaddingBottom, isSelected ? mPtTextSelected
				: mPtText);
	}

	private void drawExtraTab(Tab tab, float left, float top, Canvas canvas) {
		// left = getExtraTabLeft(id);
		// top = getExtraTabTop(id);
		// tab = mExtraTabList.get(id);
		bmpDummy = isSelected ? tab.iconSelected : tab.icon;
		// isSelected = id == mSelectedExtraPosition;
		if (bmpDummy != null)
			canvas.drawBitmap(bmpDummy, left + mSqWidth / 2 - bmpDummy.getWidth() / 2, top + mExtraTabHeight
					- mIconPaddingBottom - bmpDummy.getHeight(), mPtIcon);
		canvas.drawText(tab.title, left + mExtraTabWidth / 2, top + mExtraTabHeight - mTitlePaddingBottom, mPtText);
		Log.i("drawextra", "top = " + top);

	}

	private void drawIndicator(Canvas canvas) {
		top = mHeight - mTabHolderHeight + mIndicatorHeight / 2;
		mPtLine.setStrokeWidth(mIndicatorHeight);
		canvas.drawLine(0, top, mWidth, top, mPtLine);
		canvas.drawLine(mIndicatorLeft, top, mIndicatorLeft + mSqWidth, top, mPtIndicator);
	}

	public void flipTo(int id, boolean skipAnim) {
		Log.i("flipto", "flipto " + id);
		if (id >= 0) {
			if (id != mSelectedPosition && id < mNumTabs) {
				if (mListener != null) {
					mListener.onTabSelected(id);
				}
				mSelectedExtraPosition = -1;
			}
			mHandler.removeCallbacks(mIndicatorRunner);
			if (skipAnim) {
				mIndicatorLeft = getLeft(id);
				mSelectedPosition = id;
			} else {
				mHandler.postDelayed(mIndicatorRunner.runTo(id), INDICATOR_DELAY);
			}
			setShouldShowExtraPanel(false);

			tryShrinkDropDownAndExtraPanels();
			invalidate();
		}
	}

	private IndicatorRunner mIndicatorRunner = new IndicatorRunner();

	private class IndicatorRunner implements Runnable {
		private int id;

		public IndicatorRunner runTo(int id) {
			this.id = id;
			return this;
		}

		@Override
		public void run() {
			mIndicatorScroller.abortAnimation();
			int t = (int) Math.abs(id - mSelectedPosition) * 400;
			mIndicatorScroller.startScroll((int) mIndicatorLeft, 0, (int) (getLeft(id) - mIndicatorLeft), 0, t);
			mSelectedPosition = id;
			invalidate();
		}

	}

	private boolean isInTabHolder(float y) {
		return shouldShowExtraPanel ? y > mExtraPanelTop : y > mHeight - mTabHolderHeight;
	}

	private boolean shouldInterceptTouchEvent(float y) {
		return shouldShowExtraPanel || y > mHeight - mTabHolderHeight;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {

		return mDetector.onTouchEvent(event) || shouldInterceptTouchEvent(event.getY())
				|| super.dispatchTouchEvent(event);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		this.mWidth = w;
		this.mHeight = h;
		updateSqSize();
		updateExtraTabPosition();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	//
	// @Override
	// public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	// setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mHeight);
	// }

	public interface OnTabSelectedListener {
		public void onTabSelected(int itemId);
	}

}
