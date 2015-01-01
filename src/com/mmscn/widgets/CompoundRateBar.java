package com.mmscn.widgets;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;

import com.mmscn.utils.ViewController;
import com.mmscn.widgets.RateBar.OnRateChangedListener;

public class CompoundRateBar extends ViewController {
	private final static float LAYOUT_DELAY = 0.2F;
	private final static int ANIM_LOCK_DURATION = 2000;

	private final String mMainTitle;
	private final RateItem[] mRateItems;
	private final RateBar mMainRateBar;
	private final RateBar[] mRateBars;

	private OnCompoundRateChangedListener mListener;
	private List<Reference<TextView>> mTitleTvRef;
	private Reference<TextView> mMainTitleTv;
	private final Handler mHandler = new Handler(Looper.getMainLooper());
	private boolean isInAnim = false;

	public CompoundRateBar(int resId, int bgResId, float totalRate,
			String mainTitle, RateItem... items) {
		this.mMainTitle = mainTitle;
		this.mRateItems = items;
		mMainRateBar = new RateBar(totalRate);
		final int size = items.length;
		mRateBars = new RateBar[size];
		for (int i = 0; i < size; i++) {
			mRateBars[i] = new RateBar(totalRate, resId, bgResId)
					.enableReatTimeFeedback(true);
		}

		mMainRateBar.setOnRateChangedListener(new OnRateChangedListener() {

			@Override
			public void onRateChanged(float currRate) {
				String mainTitle = mMainTitle
						+ ": "
						+ (0 == currRate ? "未评价" : String.format("%.1f",
								currRate));
				mMainTitleTv.get().setText(mainTitle);
				for (int i = 0; i < mRateBars.length; i++) {
					RateBar bar = mRateBars[i];
					bar.setRate(currRate);
					String title = mRateItems[i].title
							+ ": "
							+ (0 == currRate ? "未评价" : String.format("%.0f",
									currRate));
					mTitleTvRef.get(i).get().setText(title);
				}
				if (null != mListener) {
					mListener.onCompoundRateChanged(getRate());
				}
			}
		});
		for (int i = 0; i < mRateBars.length; i++) {
			final int id = i;
			RateBar bar = mRateBars[i];
			bar.setOnRateChangedListener(new OnRateChangedListener() {

				@Override
				public void onRateChanged(float currRate) {
					int weightSum = 0;
					float weightedRate = 0F;
					for (int i = 0; i < mRateBars.length; i++) {
						RateItem item = mRateItems[i];
						float rate = mRateBars[i].getRate();
						if (rate > 0) {
							weightSum += item.weight;
							weightedRate += item.weight * rate;
						}
					}
					float rate = weightedRate / weightSum;
					if (!Float.isNaN(rate)) {
						mMainRateBar.setRate(rate);
						String mainTitle = mMainTitle
								+ ": "
								+ (0 == rate ? "未评价" : String.format("%.1f",
										rate));
						mMainTitleTv.get().setText(mainTitle);
					}
					String title = mRateItems[id].title
							+ ": "
							+ (0 == currRate ? "未评价" : String.format("%.0f",
									currRate));
					mTitleTvRef.get(id).get().setText(title);
					if (null != mListener) {
						mListener.onCompoundRateChanged(getRate());
					}
				}
			});
		}
	}

	public CompoundRateBar(int resId, int bgResId, String mainTitle,
			String... itemTitles) {
		this(resId, bgResId, RateBar.DEFAULT_MAX_RATING, mainTitle,
				constructDefaultRateItems(itemTitles));
	}

	public CompoundRateBar(String mainTitle, String... itemTitles) {
		this(RateBar.DEFAULT_RES_ID, RateBar.DEFAULT_BG_RES_ID,
				RateBar.DEFAULT_MAX_RATING, mainTitle,
				constructDefaultRateItems(itemTitles));
	}

	private static RateItem[] constructDefaultRateItems(String[] itemTitles) {
		final int size = itemTitles.length;
		RateItem[] items = new RateItem[size];
		for (int i = 0; i < size; i++) {
			items[i] = new RateItem(itemTitles[i], 1);
		}
		return items;
	}

	public CompoundRateBar setOnCompoundRateChangedListener(
			OnCompoundRateChangedListener l) {
		this.mListener = l;
		return this;
	}

	public Map<String, Float> getRate() {
		Map<String, Float> map = new HashMap<String, Float>();
		map.put(mMainTitle, mMainRateBar.getRate());
		for (int i = 0; i < mRateBars.length; i++) {
			map.put(mRateItems[i].title, mRateBars[i].getRate());
		}
		return map;
	}

	public View onCreateView(final Context context, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		final View v = inflater.inflate(R.layout.compound_rate_bar, parent,
				false);
		final ViewGroup container = (ViewGroup) v
				.findViewById(R.id.rate_main_container);
		ViewGroup item = (ViewGroup) inflater.inflate(
				R.layout.rate_bar_container, container, false);
		TextView title = (TextView) item.findViewById(android.R.id.title);
		mMainTitleTv = new WeakReference<TextView>(title);

		String mainTitle = mMainTitle
				+ ": "
				+ (0 == mMainRateBar.getRate() ? "未评价" : String.format("%.1f",
						mMainRateBar.getRate()));
		title.setText(mainTitle);
		View rateBar = mMainRateBar.createView(context, item);
		item.addView(rateBar);
		container.addView(item);
		final ViewGroup itemContainer = (ViewGroup) v
				.findViewById(R.id.rate_container);
		List<Reference<TextView>> l = new ArrayList<Reference<TextView>>();
		for (int i = 0; i < mRateBars.length; i++) {
			item = (ViewGroup) inflater.inflate(R.layout.rate_bar_container,
					itemContainer, false);
			title = (TextView) item.findViewById(android.R.id.title);
			rateBar = mRateBars[i].createView(context, item);
			l.add(new WeakReference<TextView>(title));
			String itemTitle = mRateItems[i].title
					+ ": "
					+ (0 == mRateBars[i].getRate() ? "未评价" : String.format(
							"%.0f", mRateBars[i].getRate()));
			title.setText(itemTitle);
			item.addView(rateBar);
			itemContainer.addView(item);
		}
		mTitleTvRef = l;
		v.findViewById(R.id.rate_bt_expand).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (isInAnim) {
							return;
						}
						lockAnim();
						boolean isHidden = View.VISIBLE != itemContainer
								.getVisibility();
						if (isHidden) {
							Animation a = AnimationUtils.loadAnimation(context,
									R.anim.fast_top_enter);
							a.setFillBefore(true);
							LayoutAnimationController lac = new LayoutAnimationController(
									a);
							lac.setDelay(LAYOUT_DELAY);
							itemContainer.setLayoutAnimation(lac);
							itemContainer
									.setLayoutAnimationListener(new AnimationListener() {

										@Override
										public void onAnimationStart(
												Animation animation) {

										}

										@Override
										public void onAnimationRepeat(
												Animation animation) {
											// TODO Auto-generated method stub

										}

										@Override
										public void onAnimationEnd(
												Animation animation) {
											isInAnim = false;
											try {
												((ImageView) peekView()
														.findViewById(
																R.id.rate_bt_expand))
														.setImageResource(R.drawable.expander_close_holo_light);
											} catch (NullPointerException e) {
												e.printStackTrace();
											}
										}
									});
							itemContainer.startLayoutAnimation();
							itemContainer.setVisibility(View.VISIBLE);
							final int[] refreshCount = new int[] { 0 };
							final int delay = (int) (a.getDuration() * LAYOUT_DELAY);
							mHandler.post(new Runnable() {

								@Override
								public void run() {
									try {
										RateBar bar = mRateBars[refreshCount[0]++];
										bar.setRate(bar.getRate());
										mHandler.postDelayed(this, delay);
									} catch (IndexOutOfBoundsException expected) {
									}
								}
							});
						} else {
							Animation a = AnimationUtils.loadAnimation(context,
									R.anim.fast_top_exit);
							a.setFillAfter(true);
							LayoutAnimationController lac = new LayoutAnimationController(
									a);
							lac.setDelay(LAYOUT_DELAY);
							lac.setOrder(LayoutAnimationController.ORDER_REVERSE);
							itemContainer.setLayoutAnimation(lac);
							itemContainer
									.setLayoutAnimationListener(new AnimationListener() {

										@Override
										public void onAnimationStart(
												Animation animation) {

										}

										@Override
										public void onAnimationRepeat(
												Animation animation) {
											// TODO Auto-generated method stub

										}

										@Override
										public void onAnimationEnd(
												Animation animation) {
											itemContainer
													.setVisibility(View.INVISIBLE);
											isInAnim = false;

											try {
												((ImageView) peekView()
														.findViewById(
																R.id.rate_bt_expand))
														.setImageResource(R.drawable.expander_open_holo_light);
											} catch (NullPointerException e) {
												e.printStackTrace();
											}
										}
									});
							itemContainer.startLayoutAnimation();
							itemContainer.invalidate();
						}
					}
				});
		return v;
	}

	public static class RateItem {
		public String title;
		public int weight;

		public RateItem(String title, int weight) {
			this.title = title;
			this.weight = weight;
		}
	}

	final Runnable mAnimUnlocker = new Runnable() {

		@Override
		public void run() {
			isInAnim = false;
		}
	};

	private void lockAnim() {
		isInAnim = true;
		mHandler.removeCallbacks(mAnimUnlocker);
		mHandler.postDelayed(mAnimUnlocker, ANIM_LOCK_DURATION);
	}

	public interface OnCompoundRateChangedListener {
		public void onCompoundRateChanged(Map<String, Float> rates);
	}
}
