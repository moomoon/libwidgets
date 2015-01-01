package com.mmscn.utils;

import java.util.List;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public abstract class SimpleRowAdapter implements ListAdapter {
	private List<Row> mList;

	private final DataSetObservable mDataSetObservable = new DataSetObservable();

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		mDataSetObservable.registerObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		mDataSetObservable.unregisterObserver(observer);
	}

	public void notifyDataSetChanged() {
		mDataSetObservable.notifyChanged();
	}

	public void notifyDataSetInvalidated() {
		mDataSetObservable.notifyInvalidated();
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public int getItemViewType(int position) {
		return mList.get(position).getViewType();
	}

	@Override
	public boolean isEmpty() {
		return getCount() == 0;
	}

	@Override
	public int getCount() {
		return null == mList ? 0 : mList.size();
	}

	@Override
	public Row getItem(int position) {
		return mList.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return mList.get(position).getView(convertView, parent);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	protected void setRowList(List<Row> l) {
		this.mList = l;
		notifyDataSetChanged();
	}
}
