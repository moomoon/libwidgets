/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mmscn.titlebar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.format.Time;

import com.mmscn.widgets.R;

/**
 * A custom view to draw the day of the month in the today button in the options
 * menu
 */

public class DayOfMonthDrawable extends Drawable {

	private String mDayOfMonth = "";
	private String mMonth = "";
	private float mTextSizeDay;
	private float mTextSizeMonth;
	private int mColorBgDay;
	private int mColorBgMonth;
	private int mColorTextDay;
	private int mColorTextMonth;

	private final Paint mPt;
	private String[] mMonthStrs;

	private int mDayTextHeight;
	private int mMonthTextHeight;

	private boolean shouldDrawSeperator = true;
	private TimeGetter mGetter;

	public DayOfMonthDrawable(Context c) {
		Resources res = c.getResources();

		mTextSizeDay = res.getDimension(R.dimen.date_text_day);
		mTextSizeMonth = res.getDimension(R.dimen.date_text_month);
		mColorBgDay = res.getColor(R.color.date_bg_day);
		mColorBgMonth = res.getColor(R.color.date_bg_month);
		mColorTextDay = res.getColor(R.color.date_text_day);
		mColorTextMonth = res.getColor(R.color.date_text_month);

		mMonthStrs = res.getStringArray(R.array.chinese_month);
		mPt = new Paint();
		mPt.setTextAlign(Align.CENTER);
		mPt.setAntiAlias(true);
		setToNow();
	}

	@Override
	public void draw(Canvas canvas) {
		tryGetTime();
		Rect bounds = getBounds();
		mPt.setColor(mColorBgMonth);
		canvas.drawRect(bounds.left, bounds.top, bounds.right, (bounds.top + bounds.bottom) / 2, mPt);
		mPt.setColor(mColorTextMonth);
		mPt.setTextSize(mTextSizeMonth);
		canvas.drawText(mMonth, (bounds.left + bounds.right) / 2, (bounds.top + bounds.bottom) / 4 + mMonthTextHeight
				/ 2, mPt);
		mPt.setColor(mColorBgDay);
		canvas.drawRect(bounds.left, (bounds.top + bounds.bottom) / 2, bounds.right, bounds.bottom, mPt);
		mPt.setColor(mColorTextDay);
		mPt.setTextSize(mTextSizeDay);
		canvas.drawText(mDayOfMonth, (bounds.left + bounds.right) / 2, (bounds.top + bounds.bottom * 3) / 4
				+ mDayTextHeight / 2, mPt);

		if (shouldDrawSeperator) {
			canvas.drawLine((4 * bounds.left + bounds.right) / 5, (bounds.top + bounds.bottom) / 2,
					(bounds.left + 4 * bounds.right) / 5, (bounds.top + bounds.bottom) / 2, mPt);
		}
	}

	public void setTimeGetter(TimeGetter getter) {
		this.mGetter = getter;
	}

	private void tryGetTime() {
		if (null != mGetter) {
			setTime(mGetter.getTime());
		}
	}

	@Override
	public void setAlpha(int alpha) {
		mPt.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// Ignore
	}

	@Override
	public int getOpacity() {
		return PixelFormat.UNKNOWN;
	}

	public DayOfMonthDrawable setDayOfMonth(int day) {
		mDayOfMonth = Integer.toString(day);
		mPt.setTextSize(mTextSizeDay);
		Rect r = new Rect();
		mPt.getTextBounds(mDayOfMonth, 0, mDayOfMonth.length(), r);
		mDayTextHeight = r.height();
		invalidateSelf();
		return this;
	}

	public DayOfMonthDrawable setMonth(int month) {
		mMonth = mMonthStrs[Math.min(mMonthStrs.length, Math.max(0, month))];
		mPt.setTextSize(mTextSizeMonth);
		Rect r = new Rect();
		mPt.getTextBounds(mMonth, 0, mMonth.length(), r);
		mMonthTextHeight = r.height();
		invalidateSelf();
		return this;

	}

	public DayOfMonthDrawable setTime(long t) {
		Time time = new Time(Time.getCurrentTimezone());
		time.set(t);
		setMonth(time.month);
		setDayOfMonth(time.monthDay);
		return this;
	}

	public void setToNow() {
		Time t = new Time(Time.getCurrentTimezone());
		t.setToNow();
		setDayOfMonth(t.monthDay);
		setMonth(t.month);
	}

	public interface TimeGetter {
		public long getTime();
	}
}
