package com.mmscn.utils;

import android.view.View;
import android.view.ViewGroup;

public interface Row {

	public int getViewType();

	public View getView(View convertView, ViewGroup parent);
}
