package com.mmscn.libform4n;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class AbstractFormEntity {
	private Reference<View> mViewRef;

	/**
	 * 构建表单视图
	 * 
	 * @param inflater
	 * @param parent
	 * @return
	 */
	public View createNewView(LayoutInflater inflater, ViewGroup parent) {
		View v = createView(inflater, parent);
		mViewRef = new WeakReference<View>(v);
		return v;
	}

	/**
	 * 向表单视图填充数据
	 * 
	 * @param data
	 * @param root
	 */
	public void setData(Map<String, String> data) {
		setData(data, mViewRef.get());
	}

	/**
	 * @see #createNewView
	 * @param inflater
	 * @param parent
	 * @return
	 */
	protected abstract View createView(LayoutInflater inflater, ViewGroup parent);

	/**
	 * @see #setData(Map)
	 * @param data
	 * @param root
	 */
	protected abstract void setData(Map<String, String> data, View root);

	protected View getView() {
		return mViewRef.get();
	}

}
