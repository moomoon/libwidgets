package com.mmscn.tabholder;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.mmscn.widgets.R;

public class YetAnotherTabHolder extends FrameLayout {

	private final float EXTRA_TRIANGLE_SIZE_RATIO = 0.333f;

	private float mHeight, mWidth;

	private Path mExtraTriangle;
	private float[] mExtraTrangleSides;

	private Shader mShaderBg;

	private Drawable mTabBgPressed;
	private int mMinClickInterval;

	// private final static int PROGRESS_TOTAL = 255;
	private final static int SHADE_ALPHA_MAX = 0x80;
	private final static int SHADE_ALPHA_MIN = 0;
	// private final static int ANIM_DELAY = 200;
	private int MAIN_PANEL_LIMIT_PORTRAIT;
	private int MAIN_PANEL_LIMIT_LANDSCAPE;
	private int EXTRA_PANEL_COLUMNS_PROTRAIT;
	private int EXTRA_PANEL_COLUMNS_LANDSCAPE;

	private static float TAB_BAR_HEIGHT_PORTRAIT;
	private static float TAB_BAR_HEIGHT_HORIZONTAL;

	private Bitmap iconMore;

	private List<Integer> mButtonId;

	protected float mTabHolderHeight;

	private int mMainPanelLimit;
	private float mExtraBtWidth;
	private static float mMainTabWidth;
	private float mExtraTabWidth;
	private float mExtraTabHeight;
	private float mExtraPanelHeight;
	private float mExtraPanelTitleHeight;
	private float mExtraPanelTitleText;
	private float mExtraPanelTitlePaddingHoriz;
	private float mIconPaddingBottom;
	private float mTitlePaddingBottom;
	private int mNumColumnsExtra;
	// private int mNumRowsExtra;
	private float mExtraLineMargin;
	private float mIconSize;

	private float mLineStrokeWidth;

	private Context mContext;
	private List<Tab> mTabList;
	private int mNumTabs;

	private boolean mShouldShowExtraPanel;

	private int mCurrSelectedId = -1;

	private Scroller mIndicatorScroller;
	private Scroller mShadeScroller;

	private GestureDetector mExtraDetector;
	private GestureDetector mMainDetector;
	private OnTabSelectionChangedListener mListener;

	private Paint mPtPanelBg;
	private Paint mPtText;
	private Paint mPtExtraPanelTitleText;
	private Paint mPtExtraPanelTitleBg;
	private Paint mPtLine;
	private Paint mPtIcon;

	private int mColorShade;
	private int mColorText;
	private int mColorTextSelected;
	private int mColorExtraPanelTitleBg;
	private int mColorExtraPanelTitleText;

	// private float left, top;
	private Tab tab;
	// private boolean isSelected;
	private Bitmap bmpDummy;
	private int i;

	private Handler mHandler;

	private float mTextSizeTab;

	private boolean isClickEnabled;

	private boolean isPortrait;

	private SparseArray<String> mGroupTitles;
	private SparseArray<Path> mGroupTitleBg;
	private SparseArray<float[]> mExtraPanelPosition;

	private boolean isTabEnabled = true;

	public YetAnotherTabHolder(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		setHapticFeedbackEnabled(true);
		setWillNotDraw(false);
	}

	private void init(Context context) {
		Resources res = context.getResources();

		mTabBgPressed = res.getDrawable(R.drawable.tab_bg_pressed);

		mColorText = res.getColor(R.color.tab_holder_text);
		mColorTextSelected = res.getColor(R.color.tab_holder_text_selected);
		// int colorIndicator = res.getColor(R.color.tab_holder_indicator);
		int colorPanelBg = res.getColor(R.color.tab_holder_extra_panel_bg);
		int colorLine = res.getColor(R.color.tab_holder_extra_panel_line);
		mColorShade = res.getColor(R.color.tab_holder_extra_panel_shade);
		mColorExtraPanelTitleBg = res.getColor(R.color.tab_holder_extra_panel_title_bg);
		mColorExtraPanelTitleText = res.getColor(R.color.tab_holder_extra_panel_title_text);

		mTextSizeTab = res.getDimension(R.dimen.tab_holder_text);
		TAB_BAR_HEIGHT_PORTRAIT = res.getDimension(R.dimen.tab_holder_height_portrait);
		TAB_BAR_HEIGHT_HORIZONTAL = res.getDimension(R.dimen.tab_holder_height_landscape);
		mTitlePaddingBottom = res.getDimension(R.dimen.tab_holder_title_padding_bottom);
		mIconPaddingBottom = res.getDimension(R.dimen.tab_holder_icon_padding_bottom);
		// mIndicatorStrokeWidth = res
		// .getDimension(R.dimen.tab_holder_indicator_height);
		mExtraBtWidth = res.getDimension(R.dimen.tab_holder_extra_tab_button_width);
		mExtraTabHeight = res.getDimension(R.dimen.tab_holder_extra_tab_height);
		mExtraLineMargin = res.getDimension(R.dimen.tab_holder_extra_panel_line_margin);
		mLineStrokeWidth = res.getDimension(R.dimen.tab_holder_extra_panel_line_width);
		mIconSize = res.getDimension(R.dimen.tab_holder_icon_size);
		mExtraPanelTitleHeight = res.getDimension(R.dimen.tab_holder_extra_panel_title_height);
		mExtraPanelTitleText = res.getDimension(R.dimen.tab_holder_extra_panel_title_text);
		mMinClickInterval = res.getInteger(R.integer.tab_holder_min_click_interval);

		MAIN_PANEL_LIMIT_PORTRAIT = res.getInteger(R.integer.tab_holder_main_limit_portrait);
		MAIN_PANEL_LIMIT_LANDSCAPE = res.getInteger(R.integer.tab_holder_main_limit_landscape);
		EXTRA_PANEL_COLUMNS_PROTRAIT = res.getInteger(R.integer.tab_holder_num_columns_extra_portrait);
		EXTRA_PANEL_COLUMNS_LANDSCAPE = res.getInteger(R.integer.tab_holder_num_columns_extra_landscape);

		iconMore = BitmapFactory.decodeResource(res, R.drawable.icon_more);

		mPtIcon = new Paint();
		// mPtIndicator = new Paint();
		mPtLine = new Paint();
		mPtPanelBg = new Paint();
		mPtText = new Paint();
		mPtExtraPanelTitleBg = new Paint();
		mPtExtraPanelTitleText = new Paint();

		// mPtIndicator.setColor(colorIndicator);
		mPtLine.setColor(colorLine);
		mPtPanelBg.setColor(colorPanelBg);
		mPtExtraPanelTitleBg.setColor(mColorExtraPanelTitleBg);
		mPtExtraPanelTitleText.setColor(mColorExtraPanelTitleText);

		mPtExtraPanelTitleBg.setAntiAlias(true);

		mPtText.setTextSize(mTextSizeTab);
		mPtText.setTextAlign(Align.CENTER);
		mPtText.setAntiAlias(true);

		mPtExtraPanelTitleText.setTextSize(mExtraPanelTitleText);
		mPtExtraPanelTitleText.setTextAlign(Align.CENTER);
		mPtExtraPanelTitleText.setAntiAlias(true);

		// mPtIndicator.setStrokeWidth(mIndicatorStrokeWidth);

		// mAnimState = -1;

		// extraTabButton = BitmapFactory.decodeResource(res,
		// R.drawable.ic_launcher);
		// extraTabButtonSelected = extraTabButton;

		updateOrientation();
		// mHandler = new Handler();

		mExtraDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

			Runnable clickableEnabler = new Runnable() {

				@Override
				public void run() {
					isClickEnabled = true;
				}
			};

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				if (mShouldShowExtraPanel && e.getY() < mHeight - mTabHolderHeight) {
					performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					flipTo(getExtraPanelPositionFromPoint(e.getX(), e.getY()), false);
					return true;
				}
				return false;
			}

			public boolean onDown(MotionEvent e) {
				if (mShouldShowExtraPanel && !isInTabHolder(e.getY())) {
					setShouldShowExtraPanel(false);
					invalidate();
					return true;
				}
				return false;
			}
		});
		mMainDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

			Runnable clickableEnabler = new Runnable() {

				@Override
				public void run() {
					isClickEnabled = true;
				}
			};

			//
			// @Override
			// public boolean onSingleTapUp(MotionEvent e) {
			// if (e.getY() > mHeight - mTabHolderHeight) {
			// performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			// if (!isClickEnabled)
			// return false;
			// mHandler.removeCallbacks(clickableEnabler);
			// mHandler.postDelayed(clickableEnabler, mMinClickInterval);
			// isClickEnabled = false;
			// if (e.getX() > mWidth - mExtraBtWidth) {
			// toggleExtraTabPanel();
			// } else {
			// flipTo(getMainPanelPositionFromPoint(e.getX()), false);
			// }
			// }
			//
			// // else if (mShouldShowExtraPanel) {
			// // performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			// // Log.i("flip to", "in extra panel");
			// //
			// flipTo(getExtraPanelIdFromPosition(getExtraPanelPositionFromPoint(e.getX(),
			// // e.getY()))
			// // + mMainPanelLimit, false);
			// // return true;
			// // }
			// return true;
			// }

			@Override
			public boolean onDown(MotionEvent e) {
				if (e.getY() > mHeight - mTabHolderHeight) {
					performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					if (!isClickEnabled)
						return false;
					mHandler.removeCallbacks(clickableEnabler);
					mHandler.postDelayed(clickableEnabler, mMinClickInterval);
					isClickEnabled = false;
					if (e.getX() > mWidth - mExtraBtWidth) {
						toggleExtraTabPanel();
					} else {
						flipTo(getMainPanelPositionFromPoint(e.getX()), false);
					}
					return true;
				}

				return false;
			}

		});
		mIndicatorScroller = new Scroller(context);
		// mAnimScroller = new Scroller(context);
		mShadeScroller = new Scroller(context);

		// mIndicatorSlider = new IndicatorSlider();
		// mAnimator = new Animator();
		applyShadeAlpha(SHADE_ALPHA_MIN);

		isClickEnabled = true;
		mHandler = new Handler();
	}

	public void setExtraPanelGroup(SparseArray<String> titles) {
		this.mGroupTitles = titles;
		updateTabSize();
		invalidate();
	}

	public void setTabsEnabled(boolean enabled) {
		isTabEnabled = enabled;
		invalidate();
	}

	private void updateExtraTriangle() {
		float side = mExtraBtWidth * EXTRA_TRIANGLE_SIZE_RATIO;
		// float sqrt3 = (float) Math.sqrt(3);

		float[] sides = new float[8];
		int count = 0;
		sides[count++] = mWidth - mExtraBtWidth / 2 - side / 2;
		sides[count++] = mHeight - mTabHolderHeight - 1;
		sides[count++] = mWidth - mExtraBtWidth / 2;
		sides[count++] = mHeight - mTabHolderHeight + side / 2 - 1;
		sides[count++] = mWidth - mExtraBtWidth / 2;
		sides[count++] = mHeight - mTabHolderHeight + side / 2 - 1;
		sides[count++] = mWidth - mExtraBtWidth / 2 + side / 2;
		sides[count++] = mHeight - mTabHolderHeight - 1;
		Path triangle = new Path();
		triangle.moveTo(sides[0], sides[1]);
		triangle.lineTo(sides[2], sides[3]);
		triangle.lineTo(sides[6], sides[7]);
		triangle.close();

		this.mExtraTriangle = triangle;
		this.mExtraTrangleSides = sides;
	}

	private void updateBgShader() {

		Shader shader = new LinearGradient(0, mHeight, 0, mHeight - mTabHolderHeight, new int[] { 0xffa9bbcc, 0xffc2d5e7 },
				new float[] { 0, 1f }, TileMode.CLAMP);
		this.mShaderBg = shader;
	}

	public void setButtonIdList(List<Integer> l) {
		this.mButtonId = l;
	}

	public Tab newTab() {
		Tab t = new Tab();
		getTabList().add(t);
		t.id = mTabList.size() - 1;
		mNumTabs = mTabList.size();
		updateTabSize();
		return t;
	}

	public void clearTabs() {
		mTabList = null;
		mNumTabs = 0;
	}

	public List<Tab> getTabList() {
		if (mTabList == null) {
			mTabList = new ArrayList<Tab>();
		}
		return mTabList;
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		this.mHeight = MeasureSpec.getSize(heightMeasureSpec);
		this.mWidth = MeasureSpec.getSize(widthMeasureSpec);
		updateOrientation();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		updateTabSize();
		updateExtraTriangle();
		updateBgShader();

	}

	// @Override
	// public void onSizeChanged(int w, int h, int oldw, int oldh) {
	// super.onSizeChanged(w, h, oldw, oldh);
	// if (mIndicatorScroller != null) {
	// Log.i("sliderstop", "stopped");
	// mIndicatorScroller.abortAnimation();
	// mIndicatorLeft = getLeft(getDisplayPosition(true, mCurrSelectedId));
	// invalidate();
	// }
	// }

	private void updateOrientation() {
		isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		this.mMainPanelLimit = isPortrait ? MAIN_PANEL_LIMIT_PORTRAIT : MAIN_PANEL_LIMIT_LANDSCAPE;
		this.mTabHolderHeight = isPortrait ? TAB_BAR_HEIGHT_PORTRAIT : TAB_BAR_HEIGHT_HORIZONTAL;
		this.mNumColumnsExtra = isPortrait ? EXTRA_PANEL_COLUMNS_PROTRAIT : EXTRA_PANEL_COLUMNS_LANDSCAPE;

		// setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(),
		// (int) mTabHolderHeight);
		requestLayout();
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		// if (mIndicatorScroller.computeScrollOffset()) {
		// mIndicatorLeft = mIndicatorScroller.getCurrX();
		// invalidate();
		// }
		if (mShadeScroller.computeScrollOffset()) {
			applyShadeAlpha(mShadeScroller.getCurrX());
			invalidate();
		}
		// if (mAnimScroller.computeScrollOffset()) {
		// applyAnim(mAnimScroller.getCurrX());
		// invalidate();
		// } else if (isInAnim()) {
		// // applyAnim(PROGRESS_TOTAL);
		// mOffsetX = 0;
		// mOffsetY = 0;
		// if (mAnimState == ANIM_RECOVERY || mAnimState == ANIM_REPLACE) {
		// mLastSelectedId = mCurrSelectedId;
		// }
		// Log.i("flip to", "stop at type = " + mAnimState + " offset x = "
		// + mOffsetX + "");
		// mAnimState = -1;
		//
		// invalidate();
		// }
	}

	// private void applyAnim(int progress) {
	// switch (mAnimState) {
	// case ANIM_RECOVERY:
	// mOffsetX = -progress / (float) PROGRESS_TOTAL * mMainTabWidth;
	// break;
	// case ANIM_INSERT:
	// mOffsetX = progress / (float) PROGRESS_TOTAL * mMainTabWidth
	// - mMainTabWidth;
	// break;
	// case ANIM_REPLACE:
	// mOffsetY = progress / (float) PROGRESS_TOTAL * mTabHolderHeight;
	// break;
	// }
	// }

	// private void tryShrinkDropDownAndExtraPanels() {
	// if (getExtraState() == EXTRA_BOTTOM
	// || getExtraState() == EXTRA_TRANS_TO_BOTTOM) {
	// scrollExtraPanelToTop();
	// }
	// if (getDropDownState() == STATE_BOTTOM
	// || getDropDownState() == STATE_TRANS_TO_BOTTOM) {
	// scrollDropDownPanelToTop();
	// }
	// }

	// private void abortAnim() {
	// if (isInAnim()) {
	// mAnimScroller.abortAnimation();
	// applyAnim(PROGRESS_TOTAL);
	// mAnimState = -1;
	// }
	// }

	private boolean checkButton(int id) {
		if (mButtonId != null) {
			for (Integer i : mButtonId) {
				if (i == id) {
					if (mListener != null) {
						mListener.onTabSelectionChanged(id);
					}
					setShouldShowExtraPanel(false);
					return true;
				}
			}
		}
		return false;
	}

	private class TabListenerInvoker implements Runnable {
		private int id;

		private TabListenerInvoker changeTo(int id) {
			this.id = id;
			return this;
		}

		@Override
		public synchronized void run() {
			if (mListener != null) {
				mListener.onTabSelectionChanged(id);
			}
			// new AsyncTask<Void, Void, Void>() {
			//
			// @Override
			// protected Void doInBackground(Void... params) {
			//
			// return null;
			// }
			// }.execute();
		}

	}

	private TabListenerInvoker mTabListenerInvoker = null;

	public void flipTo(final int id, boolean skipAnim) {
		Log.e("flipTo", "flipTo " + id);
		// TODO
		// apply skipAnim flag
		if (checkButton(id)) {
			return;
		}

		if (id == mCurrSelectedId || id < 0 || id >= mNumTabs) {
			return;
		}
		// if (mAnimState == ANIM_REPLACE) {
		// return;
		// }
		// mHandler.removeCallbacks(mAnimator);
		// mHandler.removeCallbacks(mIndicatorSlider);
		setShouldShowExtraPanel(false);
		// tryShrinkDropDownAndExtraPanels();
		if (id >= 0 && id < mNumTabs) {
			// if (null == mTabListenerInvoker)
			// mTabListenerInvoker = new TabListenerInvoker();
			// mHandler.removeCallbacks(mTabListenerInvoker);
			// Log.i("flipTo", "removed call back");
			// mHandler.postDelayed(mTabListenerInvoker.changeTo(id), 50);
			if (null != mListener)
				mListener.onTabSelectionChanged(id);
		}

		if (skipAnim) {
			mCurrSelectedId = id;
			// mLastSelectedId = id;
			// mIndicatorLeft = getLeft(id);
			invalidate();
			return;
		}
		if (id >= mMainPanelLimit) {
			// if (id != mLastSelectedId) {
			// if (isExtraTabSelected(false)) {
			// mHandler.postDelayed(mAnimator.doAnim(id, ANIM_REPLACE),
			// ANIM_DELAY);
			// } else {
			// mHandler.postDelayed(mAnimator.doAnim(id, ANIM_INSERT),
			// ANIM_DELAY);
			// }
			//
			// mHandler.postDelayed(mIndicatorSlider.slideTo(0), ANIM_DELAY);
			// }
		} else {
			// if (isExtraTabSelected(false)) {
			// if (mAnimState != ANIM_RECOVERY)
			// mHandler.postDelayed(mAnimator.doAnim(id, ANIM_RECOVERY),
			// ANIM_DELAY);
			// } else {
			// mLastSelectedId = id;
			// }
			//
			// mHandler.postDelayed(
			// mIndicatorSlider.slideTo(getDisplayPosition(true, id)
			// - (isExtraTabSelected(true) ? 1 : 0)), ANIM_DELAY);
		}
		mCurrSelectedId = id;

		invalidate();
		// mHandler.postDelayed(
		// mIndicatorSlider.slideTo(getDisplayPosition(id)
		// - (isExtraTabSelected() ? 1 : 0)), ANIM_DELAY);
	}

	public void toggleExtraTabPanel() {
		setShouldShowExtraPanel(!mShouldShowExtraPanel);
	}

	public boolean setShouldShowExtraPanel(boolean shouldShowExtraPanel) {
		if (shouldShowExtraPanel == mShouldShowExtraPanel) {
			return false;
		}

		// Log.i("flip to", String.format("color shade = %h", mColorShade));
		int alpha = Math.max(0, (mColorShade & 0xff000000) >> (6 * 4) & 0xff);

		// Log.i("flip to", String.format("alpha = %h", alpha));
		mShadeScroller.abortAnimation();
		mShadeScroller.startScroll(alpha, 0, (shouldShowExtraPanel ? SHADE_ALPHA_MAX : SHADE_ALPHA_MIN) - alpha, 0,
				shouldShowExtraPanel ? 600 : 3000);

		mShouldShowExtraPanel = shouldShowExtraPanel;
		invalidate();
		return true;
	}

	// private boolean isExtraTabSelected(boolean isNew) {
	// return (isNew ? mCurrSelectedId : mLastSelectedId) >= mMainPanelLimit;
	// }

	private boolean isExtraTab(boolean isNew, int id) {
		return id >= mMainPanelLimit;
	}

	private boolean isInTabHolder(float y) {
		return y > mHeight - mTabHolderHeight - (mShouldShowExtraPanel ? mExtraPanelHeight : 0);
	}

	private void applyShadeAlpha(int alpha) {
		mColorShade &= 0xffffff;
		mColorShade += Math.min(SHADE_ALPHA_MAX, Math.max(SHADE_ALPHA_MIN, alpha)) << (4 * 6);
	}

	// private int getDisplayPosition(boolean isNew, int id, int threshold) {
	// if (isExtraTabSelected(isNew)) {
	// if (id == threshold) {
	// return 0;
	// } else if (id < threshold) {
	// return id + 1;
	// } else {
	// return id;
	// }
	// } else {
	// return id;
	// }
	// }
	//
	// private int getDisplayPosition(boolean isNew, int id) {
	// return getDisplayPosition(isNew, id, isNew ? mCurrSelectedId
	// : mLastSelectedId);
	// }

	// private int getIdFromPosition(boolean isNew, int pos) {
	// if (pos < 0) {
	// return -1;
	// }
	// int id;
	// if (isExtraTabSelected(isNew)) {
	// if (pos == 0)
	// id = mLastSelectedId;
	// else if (pos <= mLastSelectedId) {
	// id = pos - 1;
	// } else {
	// id = pos;
	// }
	// } else {
	// id = pos;
	// }
	// Log.i("extrapanel", "id = " + id);
	// return id;
	// }

	private int getDisplayPosition(int id) {
		if (id < mMainPanelLimit - 1) {
			return id;
		} else if (id == mCurrSelectedId) {
			return mMainPanelLimit - 1;
		} else if (id < mCurrSelectedId) {
			return id + 1;
		}
		return id;
	}

	public int getMainPanelPositionFromPoint(float x) {
		float left;
		for (i = 0; i < mNumTabs; i++) {
			left = getLeft(i);
			if (x > left && x < left + mMainTabWidth) {
				if (i == mMainPanelLimit - 1 && mCurrSelectedId >= mMainPanelLimit)
					return mCurrSelectedId;

				return i;
			}
		}
		return -1;
	}

	private int getExtraPanelIdFromPosition(int pos) {
		if (pos == -1)
			return Integer.MIN_VALUE;
		if (pos + mMainPanelLimit <= mCurrSelectedId)
			return pos - 1;
		return pos;
	}

	private int getExtraPanelPositionFromPoint(float x, float y) {
		float touchX = x, touchY = y - mHeight + mTabHolderHeight + mExtraPanelHeight;
		for (i = 0; i < mNumTabs; i++) {
			float[] pos = mExtraPanelPosition.get(i);
			if (touchX > pos[0] && touchX < pos[0] + mExtraTabWidth && touchY > pos[1] && touchY < pos[1] + mExtraTabHeight) {
				return i;
			}
		}
		return -1;
	}

	private float getLeft(int id) {
		return id * mMainTabWidth;
	}

	private float getExtraTabLeft(int extraId) {
		return mExtraTabWidth * (extraId % mNumColumnsExtra);
	}

	private float getExtraTabTop(int extraId) {
		return mHeight - mTabHolderHeight - mExtraPanelHeight + mExtraTabHeight * (extraId / mNumColumnsExtra);
	}

	public void setMainPanelLimit(int mainPanelLimit) {
		this.mMainPanelLimit = mainPanelLimit;
		updateTabSize();
	}

	public void setOnTabSelectionChangedListener(OnTabSelectionChangedListener l) {
		this.mListener = l;
	}

	private void updateTabSize() {
		mMainTabWidth = (mWidth - mExtraBtWidth) / mMainPanelLimit;
		mExtraTabWidth = mWidth / mNumColumnsExtra;
		mExtraPanelPosition = new SparseArray<float[]>();
		mGroupTitleBg = new SparseArray<Path>();
		int top = 0, column = 0;
		for (int i = 0; i < mNumTabs; i++, column++) {
			if (column == mNumColumnsExtra) {
				column = 0;
				top += mExtraTabHeight;
			}
			if (null != mGroupTitles && mGroupTitles.indexOfKey(i) >= 0) {
				if (column != 0) {
					top += mExtraTabHeight;
					column = 0;
				}
				String title = mGroupTitles.get(i);
				Path p = null;
				if (null != title) {
					p = new Path();
					Rect r = new Rect();
					mPtExtraPanelTitleText.getTextBounds(title, 0, title.length(), r);
					int width = r.width();
					p.moveTo(mWidth / 2 - width / 2, top);
					p.lineTo(mWidth / 2 + width / 2, top);
					p.arcTo(new RectF(mWidth / 2 + width / 2 - mExtraPanelTitleHeight / 2, top, mWidth / 2 + width / 2
							+ mExtraPanelTitleHeight / 2, top + mExtraPanelTitleHeight), -90, 180);

					p.lineTo(mWidth / 2 - width / 2, top + mExtraPanelTitleHeight);
					p.arcTo(new RectF(mWidth / 2 - width / 2 - mExtraPanelTitleHeight / 2, top, mWidth / 2 - width / 2
							+ mExtraPanelTitleHeight / 2, top + mExtraPanelTitleHeight), 90, 180);
					p.close();
					top += mExtraPanelTitleHeight;
				}
				mGroupTitleBg.append(i, p);
			}
			mExtraPanelPosition.append(i, new float[] { column * mExtraTabWidth, top });
		}
		this.mExtraPanelHeight = top + mExtraTabHeight;
	}

	private boolean shouldInterceptTouchEvent(MotionEvent e) {
		switch (e.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			return false;
		}
		return mShouldShowExtraPanel && e.getY() < mHeight - mTabHolderHeight;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final boolean dispatchEvent = mExtraDetector.onTouchEvent(event);
		return dispatchEvent || shouldInterceptTouchEvent(event) || super.dispatchTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (isTabEnabled) {
			return mMainDetector.onTouchEvent(event) // ||
			// shouldIntercepotTouchEvent(event)
			|| true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (isTabEnabled) {

			mPtPanelBg.setColor(0xfff1f4f4);

			if (mShouldShowExtraPanel) {
				if (mCurrSelectedId < mMainPanelLimit - 1) {
					mTabBgPressed.setBounds((int) (mWidth - mExtraBtWidth), (int) (mHeight - mTabHolderHeight), (int) (mWidth),
							(int) (mHeight));
				} else {
					mTabBgPressed.setBounds((int) (getLeft(mMainPanelLimit - 1)), (int) (mHeight - mTabHolderHeight),
							(int) (mWidth), (int) (mHeight));
				}
				mTabBgPressed.draw(canvas);
			} else {
				canvas.save();
				canvas.clipRect(mWidth - mExtraBtWidth, mHeight - mTabHolderHeight, mWidth, mHeight);
				canvas.drawPaint(mPtPanelBg);
				canvas.restore();
			}
			canvas.drawBitmap(iconMore, mWidth - mExtraBtWidth / 2 - iconMore.getWidth() / 2, mHeight - mTabHolderHeight / 2
					- iconMore.getHeight() / 2, mPtIcon);
			drawMainPanel(canvas);
			if (!mShouldShowExtraPanel) {
				mPtLine.setStrokeWidth(2);
				canvas.drawLine(0, mHeight - mTabHolderHeight, mWidth, mHeight - mTabHolderHeight, mPtLine);
			}
		}
	}

	@Override
	public void dispatchDraw(Canvas canvas) {

		super.dispatchDraw(canvas);
		drawShade(canvas);

		mPtPanelBg.setColor(0xfff1f4f4);

		mPtPanelBg.setShader(null);
		if (mShouldShowExtraPanel) {
			drawExtraPanel(canvas);
			mPtLine.setStrokeWidth(2);
			mPtPanelBg.setStyle(Style.FILL);
			canvas.drawPath(mExtraTriangle, mPtPanelBg);
			canvas.drawLines(mExtraTrangleSides, mPtLine);
			canvas.drawLine(0, mHeight - mTabHolderHeight, mWidth - mExtraBtWidth / 2 - EXTRA_TRIANGLE_SIZE_RATIO / 2
					* mExtraBtWidth, mHeight - mTabHolderHeight, mPtLine);
			canvas.drawLine(mWidth - mExtraBtWidth / 2 + EXTRA_TRIANGLE_SIZE_RATIO / 2 * mExtraBtWidth, mHeight
					- mTabHolderHeight, mWidth, mHeight - mTabHolderHeight, mPtLine);
		}
	}

	public boolean isExtraPanelOpen() {
		return mShouldShowExtraPanel;
	}

	private void drawShade(Canvas canvas) {
		canvas.drawColor(mColorShade);
	}

	private void drawExtraPanel(Canvas canvas) {
		float top = mHeight - mTabHolderHeight - mExtraPanelHeight;
		canvas.drawRect(0, top, mWidth, mHeight - mTabHolderHeight, mPtPanelBg);
		for (int i = 0; i < mNumTabs; i++) {
			drawAsExtraTab(i, canvas);
		}

		canvas.save();
		canvas.translate(0, -mTabHolderHeight - mExtraPanelHeight + mHeight);
		if (null != mGroupTitles && mGroupTitles.size() > 1) {
			for (int i = 0; i < mGroupTitles.size(); i++) {
				String title = mGroupTitles.valueAt(i);
				Path bg = mGroupTitleBg.valueAt(i);
				if (null != title && null != bg) {
					canvas.drawPath(bg, mPtExtraPanelTitleBg);
					canvas.drawText(title, mWidth / 2, mExtraPanelPosition.get(mGroupTitles.keyAt(i))[1] - mExtraPanelTitleHeight
							/ 2 - mPtExtraPanelTitleText.ascent() / 2 - 3, mPtExtraPanelTitleText);
				}
			}
		}
		canvas.restore();

	}

	private void drawMainPanel(Canvas canvas) {
		int i;
		if (mNumTabs > 0)
			for (i = 0; i <= mNumTabs; i++) {
				drawAsMainTabAtPosition(i, getDisplayPosition(i), mCurrSelectedId == i, canvas);
			}
	}

	private void drawAsExtraTab(int id, Canvas canvas) {
		int pos = id;
		float[] position = mExtraPanelPosition.get(pos);
		float left = position[0];
		float top = position[1] + mHeight - mTabHolderHeight - mExtraPanelHeight;

		tab = mTabList.get(id);
		bmpDummy = mCurrSelectedId == pos ? tab.iconSelected : tab.iconExtra;
		mPtText.setColor(mColorText);
		if (bmpDummy != null)
			canvas.drawBitmap(bmpDummy, left + mExtraTabWidth / 2 - bmpDummy.getWidth() / 2, top + mExtraTabHeight
					- mIconPaddingBottom - bmpDummy.getHeight(), mPtIcon);
		canvas.drawText(tab.title, left + mExtraTabWidth / 2, top + mExtraTabHeight - mTitlePaddingBottom, mPtText);
	}

	private void drawAsMainTabAtPosition(int id, int pos, boolean isSelected, Canvas canvas) {
		if (pos >= mMainPanelLimit) {
			return;
		}

		float left = getLeft(pos);
		canvas.save();
		canvas.clipRect(left, mHeight - mTabHolderHeight, left + mMainTabWidth, mHeight);

		if (isSelected) {
			if (!(pos == mMainPanelLimit - 1 && mShouldShowExtraPanel))
				mTabBgPressed.setBounds((int) left, (int) (mHeight - mTabHolderHeight), (int) (left + mMainTabWidth),
						(int) mHeight);
			mTabBgPressed.draw(canvas);
		} else
			canvas.drawPaint(mPtPanelBg);
		tab = mTabList.get(id);
		bmpDummy = isSelected ? tab.iconSelected : tab.icon;
		mPtText.setColor(isSelected ? mColorTextSelected : mColorText);
		if (isPortrait) {
			if (bmpDummy != null)
				canvas.drawBitmap(bmpDummy, left + mMainTabWidth / 2 - bmpDummy.getWidth() / 2, mHeight - mIconPaddingBottom
						- bmpDummy.getHeight(), mPtIcon);
			canvas.drawText(tab.title, left + mMainTabWidth / 2, mHeight - mTitlePaddingBottom, mPtText);
		} else {
			if (bmpDummy != null) {
				canvas.drawBitmap(bmpDummy, left, mHeight - mTabHolderHeight / 2
						- bmpDummy.getHeight() / 2, mPtIcon);
			}
			canvas.drawText(tab.title, left + mMainTabWidth * 2 / 3, mHeight - mTabHolderHeight / 2 + mTextSizeTab / 2 - 3,
					mPtText);
		}
		canvas.restore();
	}

	// private class IndicatorSlider implements Runnable {
	// private int position;
	//
	// private IndicatorSlider slideTo(int pos) {
	// Log.i("slideto", "slide to id = " + pos);
	// if (pos >= 0) {
	// this.position = pos;
	// }
	// return this;
	// }
	//
	// @Override
	// public void run() {
	// if (position > mMainPanelLimit) {
	// return;
	// }
	// mIndicatorScroller.abortAnimation();
	// int distance = (int) (getLeft(position) - mIndicatorLeft);
	// int t = Math.abs(distance * 2);
	// mIndicatorScroller.startScroll((int) mIndicatorLeft, 0, distance,
	// 0, t);
	// // mSelectedId = position;
	// // mSelectedId = getIdFromPosition(position);
	// // isIndicatorSliding = true;
	// invalidate();
	// }
	// }

	// private class Animator implements Runnable {
	// private int id;
	// private int type;
	//
	// private Animator doAnim(int id, int type) {
	// if (id >= 0) {
	// this.id = id;
	// }
	// if (type >= 0) {
	// this.type = type;
	// }
	// return this;
	// }
	//
	// @Override
	// public void run() {
	// // applyAnim(PROGRESS_TOTAL);
	// mAnimScroller.abortAnimation();
	// mAnimState = type;
	// mAnimScroller.startScroll(0, 0, PROGRESS_TOTAL, 0, 500);
	// if (type != ANIM_RECOVERY && type != ANIM_REPLACE) {
	// mLastSelectedId = id;
	// }
	// invalidate();
	// }
	//
	// }

	public class Tab {
		public Bitmap icon;
		public Bitmap iconSelected;
		public Bitmap iconExtra;
		public String title;
		private int id;

		public Tab setIcon(Bitmap icon) {
			Bitmap scaledIcon = Bitmap.createScaledBitmap(icon, (int) mIconSize, (int) mIconSize, false);
			this.icon = scaledIcon;
			if (iconSelected == null) {
				iconSelected = scaledIcon;
			}
			if (iconExtra == null) {
				iconExtra = scaledIcon;
			}
			return this;
		}

		public Tab setIconSelected(Bitmap iconSelected) {
			Bitmap scaledIcon = Bitmap.createScaledBitmap(iconSelected, (int) mIconSize, (int) mIconSize, false);
			this.iconSelected = scaledIcon;
			return this;
		}

		public Tab setIconExtra(Bitmap iconSelected) {
			Bitmap scaledIcon = Bitmap.createScaledBitmap(iconSelected, (int) mIconSize, (int) mIconSize, false);
			this.iconExtra = scaledIcon;
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

	public interface OnTabSelectionChangedListener {
		public void onTabSelectionChanged(int itemId);
	}
}
