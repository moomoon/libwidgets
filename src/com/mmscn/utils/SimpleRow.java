package com.mmscn.utils;

import java.lang.reflect.Constructor;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.mmscn.utils.ViewHolder.Ignore;

public abstract class SimpleRow implements Row {
	private Constructor<? extends ViewHolder> mHolderConstructor;

	@Override
	public View getView(View convertView, ViewGroup parent) {
		View v = null;
		ViewHolder holder = null;
		if (null == convertView) {
			v = onCreateView(parent);
			holder = createViewHolder(v);
			v.setTag(holder);
		} else {
			v = convertView;
			holder = (ViewHolder) v.getTag();
		}
		setupView(holder, v);
		return v;
	}

	protected abstract View onCreateView(ViewGroup parent);

	protected void setupView(ViewHolder h, View v) {
	};

	protected Class<? extends ViewHolder> getViewHolderClass() {
		return null;
	}

	protected boolean skipViewHolder() {
		return false;
	}

	protected ViewHolder createViewHolder(View v) {
		if (skipViewHolder())
			return null;
		final int viewType = getViewType();
		if (null == mHolderConstructor) {
			Class<? extends ViewHolder> clazz = getViewHolderClass();
			if (null == clazz) {
				for (Class<?> c : getClass().getDeclaredClasses()) {
					if (!c.isAnnotationPresent(Ignore.class) && ViewHolder.class.isAssignableFrom(c)) {
						clazz = (Class<? extends ViewHolder>) c;
						break;
					}
				}
			}
			NoSuchMethodException ex = null;
			try {
				// static constructor
				mHolderConstructor = clazz.getDeclaredConstructor(View.class);
			} catch (NoSuchMethodException e) {
				ex = e;
			}
			if (null != ex) {
				try {
					// dynamic constructor
					mHolderConstructor = clazz.getDeclaredConstructor(getClass(), View.class);
				} catch (NoSuchMethodException e) {
					Log.w("NoSuchMethod", ex.getLocalizedMessage());
					ex.printStackTrace();
				}
			}
			try {
				mHolderConstructor.setAccessible(true);
			} catch (NullPointerException e) {
				e.printStackTrace();
				Log.e("SimpleRow",
						"You should generally define a subclass of ViewHolder by either the Class<? extends ViewHolder>getViewHolderClass(int) or "
								+ "including the class in your SimpleBaseAdapter subclass.");
			}
		}
		ViewHolder holder = null;
		try {
			if (mHolderConstructor.getParameterTypes().length > 1) {
				holder = mHolderConstructor.newInstance(this, v);
			} else {
				holder = mHolderConstructor.newInstance(v);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return holder;
	}

}
