package com.mmscn.utils;

import android.view.View;

public abstract class SimpleClickAdapter<T> extends SimpleBaseAdapter<T> {

	private SimpleOnItemClickListener mListener;

	public void setSimpleOnItemClickListener(SimpleOnItemClickListener l) {
		this.mListener = l;
	}

	protected abstract void onSetupView(int position, ViewHolder h, View v);

	@Override
	protected final void setupView(final int position, ViewHolder h, View v) {
		onSetupView(position, h, v);
		v.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (null != mListener)
					mListener.onItemClicked(getItem(position));
			}
		});
	}

	public interface SimpleOnItemClickListener {
		public void onItemClicked(Object item);
	}

}
