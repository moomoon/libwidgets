package com.mmscn.widgets;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public class FragmentListAdapter implements ListAdapter {

	private FragmentPagerAdapter mWrappedAdapter;
	private FragmentActivity mContext;
	private DataSetObserver mObserver = new DataSetObserver() {
		public void onChanged() {
			NotifyDataSetChanged();
		};
	};
	private DataSetObservable mObserverable = new DataSetObservable();

	public FragmentListAdapter(FragmentPagerAdapter adapter, FragmentActivity context) {
		this.mWrappedAdapter = adapter;
		this.mContext = context;
		mWrappedAdapter.registerDataSetObserver(mObserver);
	}

	protected FragmentPagerAdapter getWrappedAdapter() {
		return mWrappedAdapter;
	}
	
	public void NotifyDataSetChanged() {
		mObserverable.notifyChanged();
	}

	@Override
	public int getCount() {
		return mWrappedAdapter.getCount();
	}

	@Override
	public Object getItem(int position) {
		return mWrappedAdapter.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return mWrappedAdapter.getItemId(position);
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = null;
		final FragmentManager fm = mContext.getSupportFragmentManager();
		Fragment f = fm.findFragmentByTag("" + position);
		if (null == convertView) {

			if (null != f) {
				fm.beginTransaction().remove(f);
				fm.executePendingTransactions();
			}
			f = mWrappedAdapter.getItem(position);
			fm.beginTransaction().add(f, "" + position).commit();
			fm.executePendingTransactions();
			v = f.getView();
		} else {
			v = f.getView();
		}
		return v;
	}

	@Override
	public int getViewTypeCount() {
		return getCount();
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		return getCount() == 0;
	}

	public void notifyDataSetChanged() {
		mObserverable.notifyChanged();
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		mObserverable.registerObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		mObserverable.unregisterObserver(observer);
	}

	@Override
	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

}
