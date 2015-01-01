package com.mmscn.widgets;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.mmscn.utils.ViewController;

public class RateBar extends ViewController implements OnTouchListener {
	public final static float DEFAULT_MAX_RATING = 5;
	public final static int DEFAULT_RES_ID = android.R.drawable.btn_star_big_on;
	public final static int DEFAULT_BG_RES_ID = android.R.drawable.btn_star_big_off;

	private float mMaxRate;
	private int mResId;
	private int mBgResId;
	private float mCurrentRate = 0F;
	private float mLastNotifiedScore = -1F;
	private OnRateChangedListener mListener;

	private List<Reference<ProportionImageView>> mPIVList;
	private boolean mRealTimeFeedback;

	public RateBar() {
		this(DEFAULT_MAX_RATING, DEFAULT_RES_ID, DEFAULT_BG_RES_ID);
	}

	public RateBar(float totalScore) {
		this(totalScore, DEFAULT_RES_ID, DEFAULT_BG_RES_ID);
	}

	public RateBar(int resId, int bgResId) {
		this(DEFAULT_MAX_RATING, resId, bgResId);
	}

	public RateBar(float totalScore, int resId, int bgResId) {
		this.mMaxRate = totalScore;
		this.mResId = resId;
		this.mBgResId = bgResId;
	}

	public RateBar setOnRateChangedListener(OnRateChangedListener l) {
		this.mListener = l;
		return this;
	}

	public float getRate() {
		return mCurrentRate;
	}

	public RateBar enableReatTimeFeedback(boolean enabled) {
		this.mRealTimeFeedback = enabled;
		return this;
	}

	public View createView(Context context, ViewGroup parent) {
		return super.createView(context, parent);
	}

	@Override
	protected View onCreateView(Context context, ViewGroup parent) {
		ViewGroup vg = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.rate_bar, parent, false);
		final int totalScore = (int) Math.ceil(mMaxRate);
		List<Reference<ProportionImageView>> l = new ArrayList<Reference<ProportionImageView>>();
		for (int i = 0; i < totalScore; i++) {
			View item = LayoutInflater.from(context).inflate(R.layout.rate_bar_item, vg, false);
			ProportionImageView piv = (ProportionImageView) item.findViewById(android.R.id.icon);
			piv.setImageResource(mResId);
			piv.setBackgroundResource(mBgResId);
			l.add(new WeakReference<ProportionImageView>(piv));
			vg.addView(item);
		}
		mPIVList = l;
		vg.setOnTouchListener(this);
		setRate(mCurrentRate);
		return vg;
	}

	private void setIntegerRate(int rate) {
		mCurrentRate = Math.max(0F, Math.min(mMaxRate, rate));
		for (int i = 0; i < mPIVList.size(); i++) {
			ProportionImageView piv = mPIVList.get(i).get();
			if (i < rate) {
				piv.setDisplayProportion(0F, 1F, false);
			} else {
				piv.setDisplayProportion(0F, 0F, false);
			}
		}
	}

	public void setRate(float rate) {
		mCurrentRate = rate;
		for (int i = 0; i < mPIVList.size(); i++) {
			ProportionImageView piv = mPIVList.get(i).get();
			float remainder = rate - i;
			piv.setDisplayProportion(0F, Math.max(0F, Math.min(1F, remainder)), remainder <= 1 && remainder >= 0);
		}
		mLastNotifiedScore = -1F;
	}

	private boolean moved = false;
	private float tempRate;

	@Override
	public boolean onTouch(View v, MotionEvent e) {
		float score = e.getX() / v.getMeasuredWidth() * mMaxRate;
		int integerRate = (int) Math.ceil(score);
		switch (e.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			moved = false;
			tempRate = mCurrentRate;
			setIntegerRate(integerRate);
			if (mRealTimeFeedback) {
				invokeListener();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			moved = true;
			setIntegerRate(integerRate);
			if (mRealTimeFeedback) {
				invokeListener();
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (!moved && integerRate == tempRate) {
				setIntegerRate(0);
			}
			invokeListener();
			break;
		}
		return true;
	}

	private void invokeListener() {
		if (mCurrentRate != mLastNotifiedScore && null != mListener) {
			mListener.onRateChanged(mCurrentRate);
			mLastNotifiedScore = mCurrentRate;
		}
	}

	public interface OnRateChangedListener {
		public void onRateChanged(float currRate);
	}

}
