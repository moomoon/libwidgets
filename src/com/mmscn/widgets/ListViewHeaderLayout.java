package com.mmscn.widgets;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ListViewHeaderLayout extends FrameLayout implements OnScrollListener {
	private final static boolean D = false;

	private int mAddPaddingTop, mAddPaddingLeft;
	private final SparseArray<Set<View>> mViewCache = new SparseArray<Set<View>>();
	private final SparseArray<Set<View>> mViewHardCache = new SparseArray<Set<View>>();
	@SuppressLint("UseSparseArrays")
	private final Map<Integer, View> mDispView = new HashMap<Integer, View>();
	private final Map<View, Integer> mDispPosMap = new HashMap<View, Integer>();
	private final Map<View, Integer> mViewTypeCache = new HashMap<View, Integer>();
	private ListView mLv;
	private OnScrollListener mOnScrollListener;
	private Set<Integer> mHeaderTypes;
	private int mScrollState = SCROLL_STATE_IDLE;
	private boolean tempLock = false;
	private final Handler mHandler = new Handler(Looper.getMainLooper());
	private boolean isDirtyTouch;
	private final static float DIRTY_SENSITITIVY = 0.5f;
	private boolean isDirtyDummyDead = false;
	private GestureDetector mDirtyDetector;
	private int mTouchCount;

	public ListViewHeaderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ListViewHeaderLayout);
		mAddPaddingTop = ta.getDimensionPixelSize(R.styleable.ListViewHeaderLayout_additionalPaddingTop, 0);
		mAddPaddingLeft = ta.getDimensionPixelSize(R.styleable.ListViewHeaderLayout_additionalPaddingLeft, 0);
		isDirtyTouch = ta.getBoolean(R.styleable.ListViewHeaderLayout_dirtyTouch, false);
		ta.recycle();
		mDirtyDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				boolean result = Math.abs((float) distanceY / distanceX) > DIRTY_SENSITITIVY;
				Log.e("testListView", "onScroll = " + result);
				return result;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

				boolean result = Math.abs((float) (e1.getY() - e2.getY()) / (e1.getX() - e2.getX())) > DIRTY_SENSITITIVY;
				Log.e("testListView", "onFling " + result);
				return result;
			}
		});
	}

	public void setOnScrollListener(OnScrollListener l) {
		this.mOnScrollListener = l;
	}

	public void setHeaderViewType(int... types) {
		Set<Integer> headerSet = new HashSet<Integer>();
		for (int type : types)
			headerSet.add(type);
		this.mHeaderTypes = headerSet;
		mDispView.clear();
		mDispPosMap.clear();
		if (null != mLv)
			updateDispView(mLv.getFirstVisiblePosition(), mLv.getLastVisiblePosition(), false);
	}

	public void setAdditionalPaddings(int paddingTop, int paddingLeft) {
		this.mAddPaddingTop = paddingTop;
		this.mAddPaddingLeft = paddingLeft;
		if (null != mLv) {
			updateDispView(mLv.getFirstVisiblePosition(), mLv.getLastVisiblePosition(), false);
		}
	}

	public void setListView(ListView lv) {
		this.mLv = lv;
		lv.setOnScrollListener(this);
		clearHeaders();
		updateDispView(lv.getFirstVisiblePosition(), lv.getLastVisiblePosition(), true);
		ListAdapter adapter = mLv.getAdapter();
		if (null != adapter) {
			adapter.registerDataSetObserver(new ListViewObserver());
		}
	}

	private void clearHeaders() {
		for (View v : mDispView.values()) {
			removeView(v);
		}
		mViewCache.clear();
		mViewTypeCache.clear();
		mDispView.clear();
		mDispPosMap.clear();
	}

	private void updateDispView(int firstVisiblePosition, int lastVisiblePosition, boolean forceUpdateView) {
		if (tempLock)
			return;
		final ListAdapter adapter = mLv.getAdapter();
		if (null == adapter) {
			dumpAllViewToHardCache();
			return;
		}
		int bottom = mLv.getHeight();
		View header = null;
		int headerId = -1;
		for (int i = lastVisiblePosition; i >= 0; i--) {
			try {
				int viewType = adapter.getItemViewType(i);
				if (mHeaderTypes.contains(viewType)) {
					if (!forceUpdateView && mDispView.containsKey(i)) {
						header = mDispView.get(i);
					} else {
						header = getAndResetView(i);
						mDispView.put(i, header);
						mDispPosMap.put(header, i);
						mViewTypeCache.put(header, viewType);
					}
					headerId = i;
					if (i <= firstVisiblePosition)
						break;
					else {
						View dummy = mLv.getChildAt(i - firstVisiblePosition);
						if (null != dummy) {
							bottom = dummy.getTop();
						}
						header = null;
					}
				}
			} catch (NullPointerException e) {
				// getItemViewType or getView might fail when getCount = 0
			} catch (IndexOutOfBoundsException e) {

			}
		}
		if (null != header) {
			bottom = Math.min(bottom, getPaddingTop() + mAddPaddingTop + header.getMeasuredHeight());
		}

		for (Entry<Integer, View> entry : mDispView.entrySet()) {
			final View normV = entry.getValue();
			final int pos = entry.getKey();
			Integer viewTypeWrapper = mViewTypeCache.get(normV);
			// viewTypeCache has been purged, the only thing we can do is to
			// forsake normV
			final int viewType = null == viewTypeWrapper ? Integer.MIN_VALUE : viewTypeWrapper;
			if (pos < headerId || pos > lastVisiblePosition || !mHeaderTypes.contains(viewType)) {

				mLv.post(new Runnable() {

					@Override
					public void run() {
						removeAndWeakCacheView(viewType, normV);

					}
				});
			} else {
				View dummy = mLv.getChildAt(pos - firstVisiblePosition);
				if (null != dummy && View.INVISIBLE != dummy.getVisibility()) {
					dummy.setVisibility(View.INVISIBLE);
				}
				normV.bringToFront();
				if (headerId == pos && headerId <= firstVisiblePosition) {
					if (D) {
						Log.e("layoutHeader " + pos, "l= " + (getPaddingLeft() + mAddPaddingLeft + header.getLeft()) + " t= "
								+ (getPaddingTop() + mAddPaddingTop + bottom - header.getMeasuredHeight()) + " r= "
								+ (getPaddingLeft() + mAddPaddingLeft + header.getMeasuredWidth()) + " b= "
								+ (getPaddingTop() + mAddPaddingTop + bottom));
					}
					if (0 == normV.getMeasuredHeight() || 0 == normV.getMeasuredWidth()) {
						if (D)
							Log.e("layoutHeader", "requestLayout");
						requestLayout();
					}
					normV.layout(getPaddingLeft() + mAddPaddingLeft + header.getLeft(), getPaddingTop() + mAddPaddingTop + bottom
							- header.getMeasuredHeight(), getPaddingLeft() + mAddPaddingLeft + header.getMeasuredWidth(),
							getPaddingTop() + mAddPaddingTop + bottom);
				} else {
					if (null != dummy) {
						if (D)
							Log.e("layoutHeader norm " + pos, "l= " + (getPaddingLeft() + mAddPaddingLeft + dummy.getLeft()) + " t= "
									+ (getPaddingTop() + mAddPaddingTop + dummy.getTop()) + " r= "
									+ (getPaddingLeft() + mAddPaddingLeft + dummy.getRight()) + " b= "
									+ (getPaddingTop() + mAddPaddingTop + dummy.getBottom()));
						normV.layout(getPaddingLeft() + mAddPaddingLeft + dummy.getLeft(),
								getPaddingTop() + mAddPaddingTop + dummy.getTop(),
								getPaddingLeft() + mAddPaddingLeft + dummy.getRight(),
								getPaddingTop() + mAddPaddingTop + dummy.getBottom());
					} else {
						if (D)
							Log.e("layoutHeader fail " + pos, "fail");
						normV.layout(0, 0, 0, 0);
					}
				}
			}
		}
	}

	private View getAndResetView(final int pos) {
		if (D)
			Log.e("layoutHeader", "getAndReset " + pos);
		View v = mDispView.get(pos);
		final ListAdapter adapter = mLv.getAdapter();
		int viewType = adapter.getItemViewType(pos);
		if (null == v)
			v = getHardCachedView(viewType);
		boolean newView = null == v;
		if (null == v)
			v = getWeakCachedView(viewType);
		v = adapter.getView(pos, v, mLv);
		ViewGroup.LayoutParams lp = v.getLayoutParams();
		if (null == lp || !(lp instanceof LayoutParams)) {
			if (D)
				Log.e("layoutHeader", "newView at " + pos);
			v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		}
		if (newView) {
			if (D)
				Log.e("layoutHeader", "addView at " + pos);
			addView(v);
		}
		final OnItemClickListener onItemClickListener = mLv.getOnItemClickListener();
		final OnItemLongClickListener onItemLongClickListener = mLv.getOnItemLongClickListener();
		final OnClickListener onClickListener = getOnClickListener(v);
		final OnLongClickListener onLongClickListener = getOnLongClickListener(v);
		v.setEnabled(true);
		v.setFilterTouchesWhenObscured(true);
		if (null != onItemClickListener || null != onClickListener)
			v.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (null != onItemClickListener)
						onItemClickListener.onItemClick(mLv, v, pos, adapter.getItemId(pos));
					if (null != onClickListener)
						onClickListener.onClick(v);
				}
			});
		if (null != onItemLongClickListener || null != onLongClickListener)
			v.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					boolean result = false;
					if (null != onItemLongClickListener)
						result |= onItemLongClickListener.onItemLongClick(mLv, v, pos, adapter.getItemId(pos));
					if (null != onLongClickListener)
						result |= onLongClickListener.onLongClick(v);
					return result;
				}
			});
		return v;
	}

	private View getHardCachedView(int viewType) {
		View v = null;
		Set<View> set = mViewHardCache.get(viewType);
		if (null != set) {
			for (View view : set) {
				v = view;
				break;
			}
			if (null != v) {
				set.remove(v);
				v.setVisibility(View.VISIBLE);
			}
		}
		return v;
	}

	private View getWeakCachedView(int viewType) {
		View v = null;

		Set<View> set = mViewCache.get(viewType);
		if (null != set)
			for (View view : set) {
				v = view;
				break;
			}
		if (null != v) {
			set.remove(v);
			v.setVisibility(View.VISIBLE);
		}

		return v;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (isDirtyTouch) {
			boolean dirty = null != mLv && mDirtyDetector.onTouchEvent(ev);
			boolean isDown = MotionEvent.ACTION_DOWN == (ev.getAction() & MotionEvent.ACTION_MASK);
			boolean isUp = MotionEvent.ACTION_UP == (ev.getAction() & MotionEvent.ACTION_MASK);
			if (isDown)
				mTouchCount = 0;
			else if (mTouchCount < 3)
				mTouchCount++;
			// min touches for onScroll to be triggered
			if (isDown || (mTouchCount < 2 && !isUp)) {
				isDirtyDummyDead = false;
				boolean result = false;
				if (null != mLv) {
					MotionEvent dummy = MotionEvent.obtain(ev);
					dummy.setLocation(ev.getX() + mAddPaddingLeft, ev.getY() + mAddPaddingTop);
					result = mLv.dispatchTouchEvent(dummy);
				}
				return super.dispatchTouchEvent(ev) || result;
			}
			MotionEvent dummy = MotionEvent.obtain(ev);
			if (dirty) {
				ev.setLocation(ev.getX() + mAddPaddingLeft, ev.getY() + mAddPaddingTop);
				boolean result = mLv.dispatchTouchEvent(ev);
				if (!result)
					if (!isDirtyDummyDead) {
						if (MotionEvent.ACTION_UP != (dummy.getAction() & MotionEvent.ACTION_MASK))
							dummy.setAction(MotionEvent.ACTION_CANCEL);
						super.dispatchTouchEvent(dummy);
						isDirtyDummyDead = true;
					}
				return result;
			} else {
				boolean result = super.dispatchTouchEvent(ev);
				if (!result)
					if (!isDirtyDummyDead) {
						if (MotionEvent.ACTION_UP != (dummy.getAction() & MotionEvent.ACTION_MASK))
							dummy.setAction(MotionEvent.ACTION_CANCEL);
						dummy.setLocation(ev.getX() + mAddPaddingLeft, ev.getY() + mAddPaddingTop);
						mLv.dispatchTouchEvent(dummy);
						isDirtyDummyDead = true;
					}
				return result;
			}
		}

		return super.dispatchTouchEvent(ev);
	}

	private void removeAndWeakCacheView(int viewType, View v) {
		Set<View> set = mViewCache.get(viewType);
		if (null == set) {
			set = Collections.newSetFromMap(new WeakHashMap<View, Boolean>());
			mViewCache.put(viewType, set);
		}
		Integer pos = mDispPosMap.get(v);
		if (null != pos) {
			mDispView.remove(pos);
			mDispPosMap.remove(v);
			mViewTypeCache.remove(v);
		}
		removeView(v);
		set.add(v);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == SCROLL_STATE_IDLE) {
			updateDispView(mLv.getFirstVisiblePosition(), mLv.getLastVisiblePosition(), false);
		}
		mScrollState = scrollState;
		if (null != mOnScrollListener)
			mOnScrollListener.onScrollStateChanged(view, scrollState);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (D)
			Log.e("layoutHeader", "onScroll first = " + firstVisibleItem + " last = " + (firstVisibleItem + visibleItemCount));
		if (SCROLL_STATE_IDLE != mScrollState)
			updateDispView(mLv.getFirstVisiblePosition(), mLv.getLastVisiblePosition(), false);
		if (null != mOnScrollListener)
			mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			onLayoutV17(changed, left, top, right, bottom);
		} else {
			onLayoutDefault(changed, left, top, right, bottom);
		}

		if (null != mLv)
			updateDispView(mLv.getFirstVisiblePosition(), mLv.getLastVisiblePosition(), false);
	}

	private Set<View> getHardCacheSet() {
		Set<View> set = Collections.newSetFromMap(new WeakHashMap<View, Boolean>());
		for (int i = 0; i < mViewHardCache.size(); i++) {
			// Integer type = mViewHardCache.keyAt(i);
			Set<View> s = mViewHardCache.valueAt(i);
			for (View v : s) {
				set.add(v);
			}
		}
		return set;
	}

	protected void onLayoutDefault(boolean changed, int left, int top, int right, int bottom) {
		final int count = getChildCount();

		final int parentLeft = getPaddingLeft();
		final int parentRight = right - left - getPaddingRight();

		final int parentTop = getPaddingTop();
		final int parentBottom = bottom - top - getPaddingBottom();
		final Set<View> hardCacheSet = getHardCacheSet();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (!hardCacheSet.contains(child) && !mDispPosMap.containsKey(child) && child.getVisibility() != GONE) {
				final LayoutParams lp = (LayoutParams) child.getLayoutParams();

				final int width = child.getMeasuredWidth();
				final int height = child.getMeasuredHeight();

				int childLeft = parentLeft;
				int childTop = parentTop;

				final int gravity = lp.gravity;

				if (gravity != -1) {
					final int horizontalGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
					final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

					switch (horizontalGravity) {
					case Gravity.LEFT:
						childLeft = parentLeft + lp.leftMargin;
						break;
					case Gravity.CENTER_HORIZONTAL:
						childLeft = parentLeft + (parentRight - parentLeft - width) / 2 + lp.leftMargin - lp.rightMargin;
						break;
					case Gravity.RIGHT:
						childLeft = parentRight - width - lp.rightMargin;
						break;
					default:
						childLeft = parentLeft + lp.leftMargin;
					}

					switch (verticalGravity) {
					case Gravity.TOP:
						childTop = parentTop + lp.topMargin;
						break;
					case Gravity.CENTER_VERTICAL:
						childTop = parentTop + (parentBottom - parentTop - height) / 2 + lp.topMargin - lp.bottomMargin;
						break;
					case Gravity.BOTTOM:
						childTop = parentBottom - height - lp.bottomMargin;
						break;
					default:
						childTop = parentTop + lp.topMargin;
					}
				}

				child.layout(childLeft, childTop, childLeft + width, childTop + height);
			}
		}
	}

	protected void onLayoutV17(boolean changed, int l, int t, int r, int b) {
		final int count = getChildCount();

		final int parentLeft = getPaddingLeft();
		final int parentRight = r - l - getPaddingRight();

		final int parentTop = getPaddingTop();
		final int parentBottom = b - t - getPaddingBottom();
		final Set<View> hardCacheSet = getHardCacheSet();
		int skipped = 0;
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (!hardCacheSet.contains(child) && !mDispPosMap.containsKey(child) && child.getVisibility() != GONE) {
				final LayoutParams lp = (LayoutParams) child.getLayoutParams();

				final int width = child.getMeasuredWidth();
				final int height = child.getMeasuredHeight();

				int childLeft;
				int childTop;

				int gravity = lp.gravity;
				if (gravity == -1) {
					gravity = Gravity.TOP | Gravity.START;
				}

				final int layoutDirection = getLayoutDirection();
				final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
				final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

				switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
				case Gravity.LEFT:
					childLeft = parentLeft + lp.leftMargin;
					break;
				case Gravity.CENTER_HORIZONTAL:
					childLeft = parentLeft + (parentRight - parentLeft - width) / 2 + lp.leftMargin - lp.rightMargin;
					break;
				case Gravity.RIGHT:
					childLeft = parentRight - width - lp.rightMargin;
					break;
				default:
					childLeft = parentLeft + lp.leftMargin;
				}

				switch (verticalGravity) {
				case Gravity.TOP:
					childTop = parentTop + lp.topMargin;
					break;
				case Gravity.CENTER_VERTICAL:
					childTop = parentTop + (parentBottom - parentTop - height) / 2 + lp.topMargin - lp.bottomMargin;
					break;
				case Gravity.BOTTOM:
					childTop = parentBottom - height - lp.bottomMargin;
					break;
				default:
					childTop = parentTop + lp.topMargin;
				}

				child.layout(childLeft, childTop, childLeft + width, childTop + height);
			} else {
				skipped++;
			}
		}
		Log.e("onLayout", "skipped view count = " + skipped);
	}

	private void checkVisibility() {
		int lastVisiblePos = mLv.getLastVisiblePosition();
		if (0 == lastVisiblePos)
			return;
		ListAdapter adapter = mLv.getAdapter();
		int firstVisiblePos = mLv.getFirstVisiblePosition();
		for (int i = firstVisiblePos; i <= lastVisiblePos; i++) {
			Exception e = null;
			try {
				int viewType = adapter.getItemViewType(i);
				if (mHeaderTypes.contains(viewType))
					continue;
			} catch (NullPointerException ex) {
				// the listView has not respond to changes in its adapter
				e = ex;
			} catch (IndexOutOfBoundsException ex) {
				e = ex;
			}
			if (null != e) {
				mLv.post(new Runnable() {

					@Override
					public void run() {
						onDataSetChanged();
					}

				});
				return;
			}
			View child = mLv.getChildAt(i - firstVisiblePos);
			if (View.VISIBLE != child.getVisibility())
				child.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Dump all views cached in mViewTypeCache, mDispPosMap and mDispView but
	 * still keep em as children of ListViewHeaderLayout.this until
	 * mHardCachePurger set in.
	 */
	private void dumpAllViewToHardCache() {
		for (View v : mDispPosMap.keySet()) {
			int type = mViewTypeCache.get(v);
			int pos = mDispPosMap.get(v);
			Set<View> set = mViewHardCache.get(type);
			if (null == set) {
				set = new HashSet<View>();
				mViewHardCache.put(type, set);
			}
			set.add(v);
			View dummy = mLv.getChildAt(pos - mLv.getFirstVisiblePosition());
			if (null != dummy)
				dummy.setVisibility(View.VISIBLE);
			v.setVisibility(null == dummy || dummy.getTop() < 0 ? View.VISIBLE : View.GONE);
		}
		mDispPosMap.clear();
		mDispView.clear();
		mViewTypeCache.clear();
		mHandler.removeCallbacks(mHardCachePurger);
		mHandler.post(mHardCachePurger);
	}

	/**
	 * Dump all hard cached views to weak cache.
	 */
	private Runnable mHardCachePurger = new Runnable() {

		@Override
		public void run() {
			if (tempLock) {
				mHandler.post(this);
				return;
			}
			for (int i = 0; i < mViewHardCache.size(); i++) {
				Integer type = mViewHardCache.keyAt(i);
				Set<View> set = mViewHardCache.valueAt(i);
				for (View v : set) {
					removeAndWeakCacheView(type, v);
				}
			}
			mViewHardCache.clear();
			Log.e("layoutHeader", "after purge childCound = " + getChildCount());
		}

	};

	private void onDataSetChanged() {
		tempLock = true;
		mLv.post(new Runnable() {

			@Override
			public void run() {
				mLv.post(new Runnable() {

					@Override
					public void run() {
						if (D)
							Log.e("layoutHeader", "onChange finished");
						tempLock = false;
						checkVisibility();
						// setListView(mLv);
						updateDispView(mLv.getFirstVisiblePosition(), mLv.getLastVisiblePosition(), true);
					}
				});
			}
		});
		dumpAllViewToHardCache();
	}

	private class ListViewObserver extends DataSetObserver {

		@Override
		public void onChanged() {

			if (D)
				Log.e("layoutHeader", "onChange");
			onDataSetChanged();
		}

		@Override
		public void onInvalidated() {
			mLv.post(new Runnable() {

				@Override
				public void run() {
					updateDispView(mLv.getFirstVisiblePosition(), mLv.getLastVisiblePosition(), true);
				}
			});
		}

	}

	public View.OnLongClickListener getOnLongClickListener(View view) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return getOnLongClickListenerV14(view);
		} else {
			return getOnLongClickListenerV(view);
		}
	}

	public View.OnLongClickListener getOnLongClickListenerV(View view) {
		View.OnLongClickListener retrievedListener = null;
		String viewStr = "android.view.View";
		Field field;

		try {
			field = Class.forName(viewStr).getDeclaredField("mOnLongClickListener");
			retrievedListener = (View.OnLongClickListener) field.get(view);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retrievedListener;
	}

	private View.OnLongClickListener getOnLongClickListenerV14(View view) {
		View.OnLongClickListener retrievedListener = null;
		String viewStr = "android.view.View";
		String lInfoStr = "android.view.View$ListenerInfo";

		Field listenerField;
		try {
			listenerField = Class.forName(viewStr).getDeclaredField("mListenerInfo");
			Object listenerInfo = null;

			if (listenerField != null) {
				listenerField.setAccessible(true);
				listenerInfo = listenerField.get(view);
			}

			Field clickListenerField = Class.forName(lInfoStr).getDeclaredField("mOnLongClickListener");
			clickListenerField.setAccessible(true);
			if (clickListenerField != null && listenerInfo != null) {
				retrievedListener = (View.OnLongClickListener) clickListenerField.get(listenerInfo);
			}

		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retrievedListener;
	}

	public View.OnClickListener getOnClickListener(View view) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return getOnClickListenerV14(view);
		} else {
			return getOnClickListenerV(view);
		}
	}

	private View.OnClickListener getOnClickListenerV(View view) {
		View.OnClickListener retrievedListener = null;
		String viewStr = "android.view.View";
		Field field;

		try {
			field = Class.forName(viewStr).getDeclaredField("mOnClickListener");
			retrievedListener = (View.OnClickListener) field.get(view);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retrievedListener;
	}

	private View.OnClickListener getOnClickListenerV14(View view) {
		View.OnClickListener retrievedListener = null;
		String viewStr = "android.view.View";
		String lInfoStr = "android.view.View$ListenerInfo";

		Field listenerField;
		try {
			listenerField = Class.forName(viewStr).getDeclaredField("mListenerInfo");
			Object listenerInfo = null;

			if (listenerField != null) {
				listenerField.setAccessible(true);
				listenerInfo = listenerField.get(view);
			}

			Field clickListenerField = Class.forName(lInfoStr).getDeclaredField("mOnClickListener");

			if (clickListenerField != null && listenerInfo != null) {
				retrievedListener = (View.OnClickListener) clickListenerField.get(listenerInfo);
			}

		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retrievedListener;
	}
}
