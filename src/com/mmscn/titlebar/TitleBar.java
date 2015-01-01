package com.mmscn.titlebar;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.mmscn.widgets.R;

public class TitleBar extends FrameLayout {
	private enum SEP_POS {
		left, right, none
	};

	private Drawable mDefaultTrayNormal = null, mDefaultTrayPressed = null;

	public final static int CENTER_REGION = 0xface;
	public final static int MAX_SUB_TITLE_EMS = 13;

	private boolean mGlowEnabled = false;
	private Drawable mDrawableGlow = null;

	protected int mHeight, mWidth;
	protected float mTitleBarHeight;
	private float mButtonWidth;
	private float mSubTitlePaddingTop;
	private float mMainTitlePaddingBottom;
	protected boolean shouldShowTitleBar;

	protected int mPaddingTopOrigin;

	private Paint mPtTitleBarBg;
	private Paint mPtTextSub;
	private Paint mPtTextMain;
	private Paint mPtButtonTitle;
	private Paint mPtButtonTitleInversed;
	private Paint mPtSelector;
	private Paint mPtSeperator;

	private String mStrMain;
	private String mStrSub;

	private float mMainStrHeight;
	private float mSubStrHeight;

	private float mMainStrWidth;
	private float mSubStrWidth;

	private float mButtonMargin;

	private float[] mButtonLeft;
	private SEP_POS[] mSepPos;
	protected List<TitleBarButton> mButtonList;

	protected OnButtonClickedListener mOnButtonClickedListener;

	private int i;
	private int mSelectedPosition = -1;

	private float mLeftEnd;
	private float mRightEnd;

	private Drawable icon;
	private Rect r;
	private String title;
	private float h, w;
	private View mMainView;

	private Drawable mBgDrawable = null;

	private boolean shouldDrawSeperator = true;

	private float mTitleInnerPadding;

	public TitleBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
		setShouldShowTitleBar(true);
	}

	public void setTitleBarBackgroundDrawable(Drawable d) {
		this.mBgDrawable = d;
		if (mBgDrawable != null) {
			Rect paddings = new Rect();
			if (!mBgDrawable.getPadding(paddings)) {
				mBgDrawable.setBounds(0, 0, mWidth, (int) mTitleBarHeight);
			} else {
				mBgDrawable.setBounds(-paddings.left, -paddings.top, mWidth + paddings.right, (int) mTitleBarHeight
						+ paddings.bottom);
			}
		}
		invalidate();
	}

	private void init(Context context, AttributeSet attrs) {
		Resources res = context.getResources();

		int colorTitleBarBg = res.getColor(R.color.title_bar_bg);
		int colorSelector = res.getColor(R.color.title_bar_selector);
		int colorSeperator = res.getColor(R.color.title_bar_button_seperator);
		int colorText = res.getColor(R.color.title_bar_button_text);
		int colorTextInversed = res.getColor(R.color.title_bar_button_text_inversed);

		mTitleBarHeight = res.getDimension(R.dimen.title_bar_height);
		mButtonWidth = res.getDimension(R.dimen.title_bar_button_width);
		mPaddingTopOrigin = getPaddingTop();

		float mainTitleSize = res.getDimension(R.dimen.title_bar_main_title);
		float subTitleSize = res.getDimension(R.dimen.title_bar_sub_title);
		float buttonTitleSize = res.getDimension(R.dimen.title_bar_button_text);
		mSubTitlePaddingTop = res.getDimension(R.dimen.title_bar_sub_title_padding_top);
		mMainTitlePaddingBottom = res.getDimension(R.dimen.title_bar_main_title_padding_bottom);
		mButtonMargin = res.getDimension(R.dimen.title_bar_button_margin);
		mTitleInnerPadding = res.getDimension(R.dimen.title_bar_padding_inner);

		mPtTitleBarBg = new Paint();
		mPtTextSub = new Paint();
		mPtTextMain = new Paint();
		mPtButtonTitle = new Paint();
		mPtButtonTitleInversed = new Paint();
		mPtSelector = new Paint();
		mPtSeperator = new Paint();

		mPtSeperator.setColor(colorSeperator);
		mPtTitleBarBg.setColor(colorTitleBarBg);
		Shader s = new LinearGradient(0, 0, 0, mTitleBarHeight, colorTitleBarBg, 0xff058ad3, TileMode.CLAMP);
		// mPtTitleBarBg.setShader(s);
		mPtSelector.setColor(colorSelector);

		mPtTextMain.setColor(colorText);
		mPtTextSub.setColor(colorText);
		mPtButtonTitle.setColor(colorText);
		mPtButtonTitleInversed.setColor(colorTextInversed);

		mPtButtonTitle.setTextSize(buttonTitleSize);
		mPtButtonTitleInversed.setTextSize(buttonTitleSize);
		mPtTextMain.setTextSize(mainTitleSize);
		mPtTextSub.setTextSize(subTitleSize);

		mPtButtonTitle.setTextAlign(Align.CENTER);
		mPtButtonTitleInversed.setTextAlign(Align.CENTER);
		mPtTextSub.setTextAlign(Align.CENTER);
		mPtTextMain.setTextAlign(Align.CENTER);

		mPtTextMain.setAntiAlias(true);
		mPtTextSub.setAntiAlias(true);
		mPtButtonTitle.setAntiAlias(true);
		r = new Rect();

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitleBar);
		int bgRes = a.getResourceId(R.styleable.TitleBar_titlebarBackground, 0);
		if (0 != bgRes)
			setTitleBarBackgroundDrawable(res.getDrawable(bgRes));
		a.recycle();
	}

	public void setGlowEnabled(boolean glowEnabled) {
		this.mGlowEnabled = glowEnabled;
		if (mGlowEnabled) {
			if (null == mDrawableGlow) {

			}
		}
	}

	public void setDefaultTray(Drawable trayNormal, Drawable trayPressed) {
		this.mDefaultTrayNormal = trayNormal;
		this.mDefaultTrayPressed = trayPressed;
	}

	public void bindMainView(int id) {
		bindMainView(findViewById(id));
	}

	public void bindMainView(View mainView) {
		this.mMainView = mainView;
		requestLayout();
	}

	protected float getActualHeight() {
		return mHeight - getPaddingTop() - getPaddingBottom();
	}

	protected float getTitleBarBottom() {
		return mPaddingTopOrigin + mTitleBarHeight;
	}

	private int positionToId(float x) {
		for (int i = 0; i < mButtonLeft.length; i++) {
			if (x >= mButtonLeft[i] && x < mButtonLeft[i] + mButtonWidth) {
				return i;
			}
		}
		return CENTER_REGION;
	}

	public void setShouldShowTitleBar(boolean shouldShowTitleBar) {
		this.shouldShowTitleBar = shouldShowTitleBar;
		setPadding(getPaddingLeft(), (int) (mPaddingTopOrigin + (shouldShowTitleBar ? mTitleBarHeight : 0)),
				getPaddingRight(), getPaddingBottom());
		invalidate();
	}

	public void setOnButtonClickedListener(OnButtonClickedListener l) {
		this.mOnButtonClickedListener = l;
	}

	public void setMainTitle(String mainTitle) {
		this.mStrMain = mainTitle;
		updateTitleHeight();
		invalidate();
	}

	public void setSubTitle(String subTitle) {
		this.mStrSub = subTitle;
		updateTitleHeight();
		invalidate();
	}

	public void setTitles(String mainTitle, String subTitle) {
		this.mStrMain = mainTitle;
		this.mStrSub = subTitle;
		updateTitleHeight();
		invalidate();
	}

	private void updateTitleHeight() {
		if (mStrMain != null) {
			mPtTextMain.getTextBounds(mStrMain, 0, mStrMain.length(), r);
			mMainStrHeight = r.height();
			mMainStrWidth = r.width();
		}
		if (mStrSub != null) {
			mPtTextSub.getTextBounds(mStrSub, 0, mStrSub.length(), r);
			mSubStrHeight = r.height();
			mSubStrWidth = r.width();
		}
	}

	protected void updateButtonPosition() {
		if (mButtonList == null) {
			return;
		}
		mLeftEnd = 0;
		mRightEnd = mWidth + mButtonMargin;
		float[] left = new float[mButtonList.size()];
		SEP_POS[] pos = new SEP_POS[mButtonList.size()];
		SEP_POS leftPos = SEP_POS.none;
		SEP_POS rightPos = SEP_POS.none;
		TitleBarButton button = null;
		for (int i = 0; i < left.length; i++) {
			button = mButtonList.get(i);
			if (button.alignLeft) {
				left[i] = mLeftEnd;
				mLeftEnd += mButtonWidth + mButtonMargin;
				pos[i] = leftPos;
				leftPos = SEP_POS.left;
			} else {
				mRightEnd -= mButtonWidth + mButtonMargin;
				left[i] = mRightEnd;
				pos[i] = rightPos;
				rightPos = SEP_POS.right;
			}
			if (button.icon != null) {
				button.icon.setBounds((int) left[i], mPaddingTopOrigin, (int) (left[i] + mButtonWidth),
						(int) (getTitleBarBottom()));
			}
		}
		mButtonLeft = left;
		mSepPos = pos;
	}

	public void removeButton(TitleBarButton button) {
		mButtonList.remove(button);
		updateButtonPosition();
	}

	public TitleBarButton newButton(String title) {
		if (mButtonList == null) {
			mButtonList = new ArrayList<TitleBarButton>();
		}
		TitleBarButton button = new TitleBarButton(title);
		button.id = mButtonList.size();
		mButtonList.add(button);
		updateButtonPosition();
		return button;
	}

	public TitleBarButton newButton(Drawable icon) {
		if (mButtonList == null) {
			mButtonList = new ArrayList<TitleBarButton>();
		}
		TitleBarButton button = new TitleBarButton(icon);
		button.id = mButtonList.size();
		mButtonList.add(button);
		updateButtonPosition();
		return button;
	}

	@Override
	public void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (shouldShowTitleBar) {
			drawTitleBarBg(canvas);
			drawTitleBar(canvas);
		}
	}

	/**
	 * @param canvas
	 */
	protected void drawTitleBarBg(Canvas canvas) {
		if (mBgDrawable != null)
			mBgDrawable.draw(canvas);
	}

	protected void drawTitleBar(Canvas canvas) {
		// canvas.drawRect(0, 0, mWidth, mTitleBarHeight, mPtTitleBarBg);
		if (mButtonList != null && mButtonList.size() > 0)
			for (i = 0; i < mButtonList.size(); i++) {
				drawButton(i, canvas);
			}
		drawTitles(canvas);
	}

	protected void drawTitles(Canvas canvas) {
		// canvas.drawRect(mLeftEnd, mPaddingTopOrigin, mRightEnd -
		// mButtonMargin,
		// mPaddingTopOrigin + mTitleBarHeight, mPtTitleBarBg);
		if (mSelectedPosition == CENTER_REGION) {
			canvas.drawRect(mLeftEnd, mPaddingTopOrigin, mRightEnd - mButtonMargin,
					mPaddingTopOrigin + mTitleBarHeight, mPtSelector);
		}
		// int center = (int) ((mLeftEnd + mRightEnd) / 2);
		int center = mWidth / 2;
		if (mStrMain != null && mStrSub != null) {
			mPtTextMain.setTextAlign(Align.LEFT);
			mPtTextSub.setTextAlign(Align.RIGHT);
			float textWidthHalf = (mMainStrWidth + mSubStrWidth) / 2 + mTitleInnerPadding;
			canvas.drawText(mStrMain, center - textWidthHalf, mPaddingTopOrigin + mTitleBarHeight / 2 + mMainStrHeight
					/ 2 - 5, mPtTextMain);
			canvas.drawText(mStrSub, center + textWidthHalf, mPaddingTopOrigin + mTitleBarHeight / 2 + mMainStrHeight
					/ 2 - 5, mPtTextSub);
		} else if (mStrMain != null) {
			mPtTextMain.setTextAlign(Align.CENTER);
			canvas.drawText(mStrMain, center, mPaddingTopOrigin + mTitleBarHeight / 2 + mMainStrHeight / 2 - 5,
					mPtTextMain);
		} else if (mStrSub != null) {
			mPtTextSub.setTextAlign(Align.CENTER);
			canvas.drawText(mStrSub, center, mPaddingTopOrigin + mTitleBarHeight / 2 + mSubStrHeight / 2 - 5,
					mPtTextSub);
		}

	}

	private void drawButton(int id, Canvas canvas) {
		TitleBarButton bt = mButtonList.get(i);

		boolean pressed = mSelectedPosition == id;
		Drawable trayDummy = pressed ? bt.trayPressed : bt.trayNormal;
		if (null != trayDummy) {
			trayDummy.setBounds((int) mButtonLeft[i], mPaddingTopOrigin, (int) (mButtonLeft[i] + mButtonWidth),
					(int) (getTitleBarBottom()));
			trayDummy.draw(canvas);
		} else if (pressed) {
			canvas.drawRect(mButtonLeft[id], mPaddingTopOrigin, mButtonLeft[i] + mButtonWidth, mPaddingTopOrigin
					+ mTitleBarHeight, mPtSelector);
		}
		if ((icon = mButtonList.get(i).icon) != null) {
			icon.draw(canvas);
		} else {
			// canvas.drawRect(mButtonLeft[id], mPaddingTopOrigin,
			// mButtonLeft[i]
			// + mButtonWidth, mPaddingTopOrigin + mTitleBarHeight,
			// mPtTitleBarBg);
			mPtButtonTitle.getTextBounds(title = mButtonList.get(i).title, 0, title.length(), r);
			// w = r.width();
			h = r.height();
			canvas.drawText(title, mButtonLeft[i] + mButtonWidth / 2, mTitleBarHeight / 2 + h / 2, pressed
					&& bt.inverseColorWhenPressed ? mPtButtonTitleInversed : mPtButtonTitle);
		}

		if (shouldDrawSeperator) {
			switch (mSepPos[i]) {
			case left:
				canvas.drawLine(mButtonLeft[i], mTitleBarHeight / 3, mButtonLeft[i], mTitleBarHeight * 2 / 3,
						mPtSeperator);
				break;
			case none:
				break;
			case right:
				canvas.drawLine(mButtonLeft[i] + mButtonWidth, mTitleBarHeight / 3, mButtonLeft[i] + mButtonWidth,
						mTitleBarHeight * 2 / 3, mPtSeperator);
				break;
			default:
				break;

			}
		}

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (ev.getY() <= mTitleBarHeight) {
				mSelectedPosition = positionToId(ev.getX());
				invalidate();
				return true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (ev.getY() > mTitleBarHeight || positionToId(ev.getX()) != mSelectedPosition) {
				mSelectedPosition = -1;
				invalidate();
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			Log.i("show drop down", "on up");
			if (mSelectedPosition >= 0) {
				if (mSelectedPosition == CENTER_REGION) {
					onCenterRegionClicked();
				} else {
					fireButton(mButtonList.get(mSelectedPosition));
				}
				mSelectedPosition = -1;
				performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				invalidate();
			}

		}
		return super.dispatchTouchEvent(ev);
	}

	protected void fireButton(TitleBarButton bt) {
		if (mOnButtonClickedListener != null) {
			mOnButtonClickedListener.onButtonClicked(bt.id);
		}
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		this.mHeight = MeasureSpec.getSize(heightMeasureSpec);
		this.mWidth = MeasureSpec.getSize(widthMeasureSpec);
		if (mBgDrawable != null)
			// mBgDrawable.setBounds(0, 0, mWidth, (int) mTitleBarHeight);
			setTitleBarBackgroundDrawable(mBgDrawable);
		// if (mMainView != null) {
		// mMainView.measure(MeasureSpec.makeMeasureSpec((int) mWidth,
		// MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
		// (int) mHeight - getPaddingBottom() - getPaddingTop(),
		// MeasureSpec.EXACTLY));
		// }
	}

	// @Override
	// public void onLayout(boolean changed, int left, int top, int right,
	// int bottom) {
	//
	// Log.i("titleBar","title bar onlayout");
	// if (mMainView != null) {
	// mMainView.layout(0, getPaddingTop(), mWidth, bottom - getPaddingBottom()
	// );
	// }
	// }

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {

		updateButtonPosition();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	public interface OnButtonClickedListener {
		public void onButtonClicked(int itemId);
	}

	public class TitleBarButton {
		private Drawable icon;
		private Drawable trayNormal, trayPressed;
		private String title;
		private boolean inverseColorWhenPressed;
		protected int id;
		private boolean alignLeft;

		public TitleBarButton(String title) {
			this.title = title;
		}

		public TitleBarButton(Drawable icon) {
			this.icon = icon;
		}

		public TitleBarButton setInverseColorWhenPressed(boolean i) {
			this.inverseColorWhenPressed = i;
			return this;
		}

		public TitleBarButton setTray(Drawable trayNormal, Drawable trayPressed) {
			this.trayNormal = trayNormal;
			this.trayPressed = trayPressed;
			return this;
		}

		public TitleBarButton setShowDefaultTray() {
			setInverseColorWhenPressed(true);
			return setTray(mDefaultTrayNormal, mDefaultTrayPressed);
		}

		public TitleBarButton setTitle(String title) {
			this.title = title;
			return this;
		}

		public TitleBarButton setIcon(Drawable icon) {
			this.icon = icon;
			updateButtonPosition();
			return this;
		}

		public TitleBarButton setAlignLeft(boolean alignLeft) {
			this.alignLeft = alignLeft;
			updateButtonPosition();
			return this;
		}

		public int getId() {
			return id;
		}

	}

	public void onCenterRegionClicked() {
		if (mOnButtonClickedListener != null) {
			mOnButtonClickedListener.onButtonClicked(CENTER_REGION);
		}
	};

}
