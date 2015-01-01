package com.mmscn.libform4n;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mmscn.widgets.R;

public abstract class FormAdapter extends BaseAdapter {
	private enum FormAdapterType {
		FormItem, FormHeader
	};

	private List<Row> mList;

	public int getItemViewType(int position) {
		return mList.get(position).getType();
	}

	public int getViewTypeCount() {
		return FormAdapterType.values().length;
	}

	public static int[] getHeaderTypes() {
		return new int[] { FormAdapterType.FormHeader.ordinal() };
	}

	public void setList(List<Map<String, String>> mapList, SparseArray<String> headerArray) {
		List<Row> l = new ArrayList<Row>();
		List<Map<String, String>> ml = new ArrayList<Map<String, String>>();
		SparseArray<String> sa = new SparseArray<String>();
		if (null != headerArray) {
			for (int i = 0; i < headerArray.size(); i++)
				sa.append(headerArray.keyAt(i), headerArray.valueAt(i));
		}
		if (null != mapList)
			ml.addAll(mapList);
		int headerPos = 0;
		for (int i = 0; i < ml.size(); i++) {
			String header = sa.get(i);
			if (null != header) {
				headerPos = i;
				l.add(new HeaderRow(header));
			}
			l.add(new ItemRow(ml.get(i)));
		}
		int i = sa.indexOfKey(headerPos);
		if (i >= 0)
			for (i++; i < sa.size(); i++)
				l.add(new HeaderRow(sa.valueAt(i)));
		mList = l;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return null == mList ? 0 : mList.size();
	}

	@Override
	public Object getItem(int position) {
		Row row = mList.get(position);
		if (row instanceof ItemRow)
			return ((ItemRow) row).map;
		return ((HeaderRow) row).header;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.e("prescriptionAdapter", "into GetView");
		parent.post(new Runnable() {

			@Override
			public void run() {
				Log.e("prescriptionAdapter", "out of GetView");
			}
		});
		return mList.get(position).getView(convertView, parent);
	}

	private abstract class Row {
		abstract int getType();

		abstract View getView(View convertView, ViewGroup parent);
	}

	protected abstract void onEdit(Map<String, String> data);

	protected abstract boolean isEditable(Map<String, String> data);

	protected abstract void onView(Map<String, String> data);

	protected abstract boolean isViewable();

	protected abstract void onDelete(Map<String, String> data);

	protected abstract boolean isDeletable(Map<String, String> data);

	protected abstract AbstractFormEntity getNewFormEntity();

	private class ItemRow extends Row {
		private Map<String, String> map;

		private ItemRow(Map<String, String> map) {
			this.map = map;
		}

		@Override
		int getType() {
			return FormAdapterType.FormItem.ordinal();
		}

		@Override
		View getView(View convertView, final ViewGroup parent) {
			View v = null;
			AbstractFormEntity entity = null;
			if (null == convertView) {
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				entity = getNewFormEntity();
				ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.list_form_item_wrapper, parent, false);
				vg.addView(entity.createNewView(inflater, vg));
				v = vg;
				v.setTag(entity);
				v.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			} else {
				v = convertView;
				entity = (AbstractFormEntity) convertView.getTag();
			}
			entity.setData(map);
			v.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					List<String> itemList = new ArrayList<String>();
					if (isEditable(map)) {
						itemList.add("修改");
					} else if (isViewable()) {
						itemList.add("查看");
					}
					if (isDeletable(map)) {
						itemList.add("删除");
					}

					if (0 == itemList.size())
						return false;
					final String[] items = new String[itemList.size()];
					itemList.toArray(items);
					new AlertDialog.Builder(v.getContext()).setItems(items, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String item = items[which];
							if (item.equals("修改")) {
								onEdit(map);
							} else if (item.equals("查看")) {
								onView(map);
							} else if (item.equals("删除")) {
								onDelete(map);
							}
						}
					}).show();
					return true;
				}
			});
			return v;
		}
	}

	private class HeaderRow extends Row {
		private String header;

		private HeaderRow(String header) {
			this.header = header;
		}

		@Override
		int getType() {
			return FormAdapterType.FormHeader.ordinal();
		}

		@Override
		View getView(View convertView, ViewGroup parent) {
			TextView tv = null;
			if (null == convertView) {
				tv = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_form_header, parent,
						false);
			} else {
				tv = (TextView) convertView;
			}
			tv.setText(header);
			return tv;
		}

	}
}
