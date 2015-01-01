package com.mmscn.utils;

import java.lang.reflect.Constructor;
import java.util.List;

import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.mmscn.utils.ViewHolder.Ignore;
import com.mmscn.widgets.ViewType;

public abstract class SimpleBaseAdapter<T> extends BaseAdapter {
	protected List<T> mList;
	private final SparseArray<Constructor<? extends ViewHolder>> mViewHolderConstructors = new SparseArray<Constructor<? extends ViewHolder>>();

	public void setList(List<T> l) {
		this.mList = l;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return null == mList ? 0 : mList.size();
	}

	@Override
	public T getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public List<T> getList() {
		return mList;
	}

	protected ViewHolder createViewHolder(int position, View v) {
		final int viewType = getItemViewType(position);
		Constructor<? extends ViewHolder> constructor = mViewHolderConstructors.get(viewType);
		if (null == constructor) {
			Class<? extends ViewHolder> clazz = getViewHolderClass(viewType);
			if (null == clazz) {
				SEARCH_CLASS: for (Class<?> c : getClass().getDeclaredClasses()) {
					if (!c.isAnnotationPresent(Ignore.class) && ViewHolder.class.isAssignableFrom(c)) {
						int[] classViewType = new int[1];
						if (c.isAnnotationPresent(ViewType.class)) {
							classViewType = c.getAnnotation(ViewType.class).value();
						}
						for (int type : classViewType) {
							if (type == viewType) {
								clazz = (Class<? extends ViewHolder>) c;
								break SEARCH_CLASS;
							}
						}
					}
				}
			}
			NoSuchMethodException ex = null;
			try {
				// static constructor
				constructor = clazz.getDeclaredConstructor(View.class);
			} catch (NoSuchMethodException e) {
				ex = e;
			}
			if (null != ex) {
				try {
					// dynamic constructor
					constructor = clazz.getDeclaredConstructor(getClass(), View.class);
				} catch (NoSuchMethodException e) {
					Log.w("NoSuchMethod", ex.getLocalizedMessage());
					ex.printStackTrace();
				}
			}
			try {
				constructor.setAccessible(true);
				mViewHolderConstructors.put(viewType, constructor);
			} catch (NullPointerException e) {
				e.printStackTrace();
				throw new RuntimeException(
						"You must define a subclass of ViewHolder by either the Class<? extends ViewHolder>getViewHolderClass(int) or "
								+ "including the class in your SimpleBaseAdapter subclass.");
			}
			Log.e("create viewHolder", "v = " + v + " clazz = " + clazz + " constructor = " + constructor);
		}
		ViewHolder holder = null;
		try {
			if (constructor.getParameterTypes().length > 1) {
				holder = constructor.newInstance(this, v);
			} else {
				holder = constructor.newInstance(v);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return holder;
	}

	protected Class<? extends ViewHolder> getViewHolderClass(int viewType) {
		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = null;
		ViewHolder holder = null;
		if (null == convertView) {
			v = createView(position, parent);
			holder = createViewHolder(position, v);
			v.setTag(holder);
		} else {
			v = convertView;
			holder = (ViewHolder) v.getTag();
		}
		setupView(position, holder, v);
		return v;
	}

	public View createView(int position, ViewGroup parent) {
		View v = onCreateView(position, parent);
		return v;
	}

	protected abstract View onCreateView(int position, ViewGroup parent);

	protected abstract void setupView(int position, ViewHolder h, View v);

}