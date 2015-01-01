package com.mmscn.titlebar;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;

public class TitleBarWithSubMenuButton extends TitleBar {
	private final static int SUB_BUTTON_POSITION = 0xff;
	private SubButtonProvider mProvider = null;
	private List<TitleBarButton> mSubButtonList = null;
	private int mMainButtonCount = 0;
	private Handler mHandler;

	public TitleBarWithSubMenuButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mHandler = new Handler();
	}

	public TitleBarButton newButton(String title) {
		if (mButtonList == null) {
			mButtonList = new ArrayList<TitleBarButton>();
		}
		TitleBarButton button = new TitleBarButton(title);
		button.id = mMainButtonCount++;
		mButtonList.add(button);
		updateButtonPosition();
		return button;
	}

	public TitleBarButton newButton(Drawable icon) {
		if (mButtonList == null) {
			mButtonList = new ArrayList<TitleBarButton>();
		}
		TitleBarButton button = new TitleBarButton(icon);
		button.id = mMainButtonCount++;
		mButtonList.add(button);
		updateButtonPosition();
		return button;
	}

	@Override
	protected void fireButton(TitleBarButton bt) {
		if (bt.id < SUB_BUTTON_POSITION) {
			if (mOnButtonClickedListener != null) {
				mOnButtonClickedListener.onButtonClicked(bt.id);
			}
		} else if (bt.id >= SUB_BUTTON_POSITION) {
			if (mProvider != null) {
				mProvider.onSubButtonClicked(bt.id - SUB_BUTTON_POSITION);
			}
		}
		invalidate();
	}

	public void setSubButtonProvider(SubButtonProvider provider) {
		if (mProvider != null)
			for (TitleBarButton b : mSubButtonList) {
				removeButton(b);
			}
		mSubButtonList = new ArrayList<TitleBarButton>();
		if (provider != null) {
			for (int i = 0; i < provider.getSubButtonCount(); i++) {
				TitleBarButton button = provider.getSubButtonAt(TitleBarWithSubMenuButton.this, i);
				button.id = mSubButtonList.size() + SUB_BUTTON_POSITION;
				mSubButtonList.add(button);
				mButtonList.add(button);
			}
			TitleBarWithSubMenuButton.this.mProvider = provider;
		}
		updateButtonPosition();
		invalidate();
	}

	public interface SubButtonProvider {
		public int getSubButtonCount();

		public TitleBarButton getSubButtonAt(TitleBar titleBar, int position);

		public void onSubButtonClicked(int position);
	}

}
