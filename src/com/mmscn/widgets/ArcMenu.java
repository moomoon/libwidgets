package com.mmscn.widgets;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

public class ArcMenu extends RelativeLayout {
	private final static int DEFAULT_ITEM_ANIM_DELAY = 50;
	private final static int DEFAULT_MAX_ALPHA = 200;
	private final static int DEFAULT_MENU_RADIUS = 400;
	private final static int DEFAULT_ITEM_ANIM_DURATION = 300;
	private final static int DEFAULT_ICON_ROTATE_DEG = 180;
	private final static int DEFAULT_MENU_START_DEG = -90;
	private final static int DEFAULT_MENU_DEG_SPAN = 180;
	private boolean mShowMenu = false;
	private OnMenuClickedListener mOnMenuClickedListener;

	private int mMaxAlpha;
	private int mItemAnimDelay;
	private int mMenuRadius;
	private int mItemAnimDuration;
	private int mIconRotateDeg;
	private int mMenuStartDeg;
	private int mMenuDegSpan;

	private List<View> mMenuItems;
	private int mAnchorX, mAnchorY;
	private Scroller mScroller;
	private int mCurrentAlpha = 0;
	private boolean mInCollapse = false;
	private final Handler mHandler = new Handler(Looper.getMainLooper());
	private boolean mSnapshotEnabled;
	private Paint mPtSnapshot;
	private Bitmap mSnapshot;
	private int mFocusLeft, mFocusTop, mFocusRight, mFocusBottom;

	public ArcMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ArcMenu);
		mMaxAlpha = ta.getInteger(R.styleable.ArcMenu_maxAlpha, DEFAULT_MAX_ALPHA);
		mItemAnimDelay = ta.getInteger(R.styleable.ArcMenu_itemAnimDelay, DEFAULT_ITEM_ANIM_DELAY);
		mMenuRadius = ta.getDimensionPixelSize(R.styleable.ArcMenu_menuRadius, DEFAULT_MENU_RADIUS);
		mItemAnimDuration = ta.getInteger(R.styleable.ArcMenu_itemAnimDuration, DEFAULT_ITEM_ANIM_DURATION);
		mIconRotateDeg = ta.getInteger(R.styleable.ArcMenu_iconRotateDegree, DEFAULT_ICON_ROTATE_DEG);
		mMenuStartDeg = ta.getInteger(R.styleable.ArcMenu_menuStartDegree, DEFAULT_MENU_START_DEG);
		mMenuDegSpan = ta.getInteger(R.styleable.ArcMenu_menuDegreeSpan, DEFAULT_MENU_DEG_SPAN);
		ta.recycle();
		mScroller = new Scroller(context);
		Drawable bg = getBackground();
		if (null != bg)
			bg.setAlpha(mCurrentAlpha);
	}

	public void setOnMenuClickedListener(OnMenuClickedListener l) {
		this.mOnMenuClickedListener = l;
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			mCurrentAlpha = mScroller.getCurrX();
			Drawable bg = getBackground();
			if (null != bg) {
				bg.setAlpha(mCurrentAlpha);
				invalidate();
			}
			if (null != mPtSnapshot) {
				mPtSnapshot.setAlpha(mCurrentAlpha * 255 / mMaxAlpha);
			}
		}
	}

	public void setSnapshotEnabled(boolean enabled) {
		this.mSnapshotEnabled = enabled;
		mPtSnapshot = enabled ? new Paint() : null;
	}

	@Override
	public void draw(Canvas canvas) {
		if (mShowMenu && !mSnapshotEnabled) {
			canvas.save();
			canvas.clipRect(mFocusLeft, mFocusTop, mFocusRight, mFocusBottom, Op.DIFFERENCE);
			super.draw(canvas);
		} else {
			super.draw(canvas);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mShowMenu) {
			// canvas.restore();
			if (mSnapshotEnabled) {
				canvas.drawBitmap(mSnapshot, mFocusLeft, mFocusTop, mPtSnapshot);
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
	}

	public void showMenu(View v, int rawAnchorX, int rawAnchorY) {
		if (mShowMenu) {
			// hideMenu();
			return;
		}
		mInCollapse = false;
		Animation a;
		mFocusLeft = v.getLeft();
		mFocusTop = v.getTop();
		mFocusRight = v.getRight();
		mFocusBottom = v.getBottom();
		ImageView icon = (ImageView) mMenuItems.get(0).findViewById(R.id.arc_menu_icon);
		final int radius = Math.min(icon.getMeasuredHeight(), icon.getMeasuredWidth()) / 2;
		final int anchorX = rawAnchorX - radius;
		final int anchorY = rawAnchorY - radius;

		if (mSnapshotEnabled) {
			Bitmap bm = Bitmap.createBitmap(mFocusRight - mFocusLeft, mFocusBottom - mFocusTop, Config.ARGB_8888);
			Canvas c = new Canvas(bm);
			v.draw(c);
			mSnapshot = bm;
		}
		for (int i = 0; i < mMenuItems.size(); i++) {
			double angle = Math.PI * mMenuStartDeg / 180 + Math.PI * mMenuDegSpan / 180 / (mMenuItems.size() - 1) * i;
			final int destX = (int) (anchorX + Math.cos(angle) * mMenuRadius);
			final int destY = (int) (anchorY + Math.sin(angle) * mMenuRadius);
			final View menu = mMenuItems.get(i);
			menu.setVisibility(View.INVISIBLE);
			if (null == menu.getParent())
				addView(menu);

			final AnimationSet set = new AnimationSet(true);
			a = new AlphaAnimation(0, 1);
			set.addAnimation(a);
			set.setDuration(mItemAnimDuration);
			a = new TranslateAnimation(Animation.ABSOLUTE, anchorX - destX, Animation.ABSOLUTE, 0, Animation.ABSOLUTE,
					anchorY - destY, Animation.ABSOLUTE, 0);
			set.addAnimation(a);
			set.setStartOffset(i * mItemAnimDelay);
			set.setFillAfter(true);
			final int animId = i;
			post(new Runnable() {

				@Override
				public void run() {
					menu.layout(destX, destY, destX + menu.getMeasuredWidth(), destY + menu.getMeasuredHeight());
					menu.bringToFront();
					menu.startAnimation(set);
					((RotateImageView) menu.findViewById(R.id.arc_menu_icon)).rotate(-mIconRotateDeg, 0,
							mItemAnimDuration * 2, animId * mItemAnimDelay);
				}
			});
		}

		mScroller.abortAnimation();
		Drawable bg = getBackground();
		if (null != bg) {
			final int alpha = mCurrentAlpha;
			mScroller.startScroll(alpha, 0, mMaxAlpha - alpha, 0);
			invalidate();
		}
		this.mAnchorX = anchorX;
		this.mAnchorY = anchorY;

		mShowMenu = true;
		setClickable(true);

	}

	public void hideMenu() {
		if (mShowMenu && !mInCollapse) {
			mInCollapse = true;
			for (int i = 0; i < mMenuItems.size(); i++) {
				final View menu = mMenuItems.get(i);
				menu.setVisibility(View.INVISIBLE);
				AnimationSet set = new AnimationSet(true);
				set.setDuration(mItemAnimDuration);
				Animation a = new AlphaAnimation(1, 0);
				set.addAnimation(a);

				a = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, mAnchorX - menu.getLeft(),
						Animation.ABSOLUTE, 0, Animation.ABSOLUTE, mAnchorY - menu.getTop());
				set.addAnimation(a);

				set.setStartOffset(i * mItemAnimDelay);

				set.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						post(new Runnable() {

							@Override
							public void run() {
								removeView(menu);
							}
						});
					}
				});
				menu.startAnimation(set);
				((RotateImageView) menu.findViewById(R.id.arc_menu_icon)).rotate(0, -mIconRotateDeg,
						mItemAnimDuration * 2, i * mItemAnimDelay);
				menu.findViewById(R.id.arc_menu_title).setVisibility(View.INVISIBLE);
			}

			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {

					mShowMenu = false;
					mInCollapse = false;
					setClickable(false);
					if (null != mSnapshot) {
						mSnapshot.recycle();
						mSnapshot = null;
					}
				}
			}, mItemAnimDelay * (mMenuItems.size() - 1) + mItemAnimDuration);

			mScroller.abortAnimation();
			Drawable bg = getBackground();
			if (null != bg) {
				final int alpha = mCurrentAlpha;
				mScroller.startScroll(alpha, 0, -alpha, 0);
				invalidate();
			}

		}
	}

	public void setMenu(int[] iconRes, String[] title) {
		if (null == mMenuItems)
			mMenuItems = new ArrayList<View>();
		final int newSize = iconRes.length;
		if (newSize != mMenuItems.size()) {
			while (newSize < mMenuItems.size()) {
				mMenuItems.remove(newSize - 1);
			}
			while (newSize > mMenuItems.size()) {
				View menu = LayoutInflater.from(getContext()).inflate(R.layout.arc_menu_item, this, false);
				menu.findViewById(R.id.arc_menu_icon).setOnTouchListener(
						new MenuTouchListener(mMenuItems.size(), (TextView) menu.findViewById(R.id.arc_menu_title)));
				mMenuItems.add(menu);
			}
		}
		for (int i = 0; i < mMenuItems.size(); i++) {
			View menu = mMenuItems.get(i);
			ImageView ivIcon = (ImageView) menu.findViewById(R.id.arc_menu_icon);
			TextView tvTitle = (TextView) menu.findViewById(R.id.arc_menu_title);
			ivIcon.setImageResource(iconRes[i]);
			tvTitle.setText(title[i]);

		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (MotionEvent.ACTION_DOWN == (event.getAction() & MotionEvent.ACTION_MASK))
			hideMenu();
		return super.onTouchEvent(event);
	}

	private class MenuTouchListener implements OnTouchListener {
		private final TextView tvTitle;
		private final int id;

		private MenuTouchListener(int id, TextView tvTitle) {
			this.id = id;
			this.tvTitle = tvTitle;
		}

		private void showTitle() {
			tvTitle.setVisibility(View.VISIBLE);
			Animation a = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
			tvTitle.startAnimation(a);
		}

		private void hideTitle() {
			Animation a = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
			tvTitle.startAnimation(a);
			a.setFillAfter(true);
			tvTitle.startAnimation(a);
		}

		private void fireListener() {
			if (null != mOnMenuClickedListener) {
				mOnMenuClickedListener.onMenuClicked(id);
			}
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				showTitle();
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				hideTitle();
				int x = (int) event.getX();
				int y = (int) event.getY();
				if (x >= 0 && x <= v.getWidth() && y >= 0 && y <= v.getHeight()) {
					hideMenu();
					fireListener();
				}
				break;
			}
			return true;
		}

	}

	public interface OnMenuClickedListener {
		public void onMenuClicked(int id);
	}

	public interface onMenuHideListener {
		public void onMenuHide();
	}
}
