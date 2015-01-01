package com.mmscn.libform4n;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.widget.ListView;

import com.mmscn.widgets.ListViewHeaderLayout;

public class FormLVHeaderLayout extends ListViewHeaderLayout {

	private ListFormCallbacks mCallbacks;
	private final FormAdapter mAdapter = new FormAdapter() {

		@Override
		protected AbstractFormEntity getNewFormEntity() {
			return mCallbacks.getNewFormEntity();
		}

		@Override
		protected void onEdit(Map<String, String> data) {
			mCallbacks.onEdit(data);
		}

		@Override
		protected boolean isEditable(Map<String, String> data) {
			return mCallbacks.isEditable(data);
		}

		@Override
		protected void onView(Map<String, String> data) {
			mCallbacks.onView(data);
		}

		@Override
		protected boolean isViewable() {
			return mCallbacks.isViewable();
		}

		@Override
		protected void onDelete(Map<String, String> data) {
			mCallbacks.onDelete(data);
		}

		@Override
		protected boolean isDeletable(Map<String, String> data) {
			return mCallbacks.isDeletable(data);
		}

	};

	public FormLVHeaderLayout(final Context context, AttributeSet attrs) {
		super(context, attrs);
		post(new Runnable() {

			@Override
			public void run() {
				ListView lv = new ListView(context);
				addView(lv, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				lv.setAdapter(mAdapter);
				lv.setVerticalScrollBarEnabled(false);
				setHeaderViewType(FormAdapter.getHeaderTypes());
				setListView(lv);
			}
		});
	}

	public void setListFormCallbacks(ListFormCallbacks callbacks) {
		this.mCallbacks = callbacks;
	}

	public void setList(List<Map<String, String>> mapList, SparseArray<String> headerArray) {
		mAdapter.setList(mapList, headerArray);
	}

	public interface ListFormCallbacks {
		public AbstractFormEntity getNewFormEntity();

		public boolean isEditable(Map<String, String> data);

		public boolean isViewable();

		public boolean isDeletable(Map<String, String> data);

		public void onEdit(Map<String, String> data);

		public void onView(Map<String, String> data);

		public void onDelete(Map<String, String> data);
	}

}
