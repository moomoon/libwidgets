package com.mmscn.calendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.widget.TextView;

import com.mmscn.widgets.R;


public class WeekBar extends TextView{
	private final static int NUM_DAYS_PER_WEEK = 7;
	private Paint mPaintLines;
	private Paint mPaintText;
	private int mColorLine;
	private int mColorBg;
	private int mHeight, mWidth;
	private int mSqWidth;
	private int mDatePaddingBottom;
	private String[] str={"日","一","二","三","四","五","六"};
	public WeekBar(Context context,AttributeSet attrs){
		super(context,attrs);
		
		init(context);
	}
	private void init(Context context){
		Resources res=context.getResources();
		mPaintLines=new Paint();
		mPaintText=new Paint();
		mColorLine=res.getColor(R.color.calendar_date_line);
		mColorBg = res.getColor(R.color.calendar_date_bg);
		float textSize = res.getDimension(R.dimen.calendar_date_text);
		mDatePaddingBottom = (int) res
		.getDimension(R.dimen.calendar_date_padding_bottom);
		mPaintLines.setColor(mColorLine);
		mPaintText.setTextSize(textSize);
		mPaintText.setTextAlign(Align.CENTER);
		mPaintText.setAntiAlias(true);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		int i;
		int left;
		for (i = 0; i <= NUM_DAYS_PER_WEEK; i++) {
			canvas.drawLine(getLeftFromColumn(i), 0, getLeftFromColumn(i),
					mHeight, mPaintLines);
		}
			
		for (i = 0; i < NUM_DAYS_PER_WEEK ; i++) {
			left = getLeftFromId(i);
			canvas.drawText(str[i], left + mSqWidth / 2, 
					mHeight - mDatePaddingBottom, mPaintText);
		}
	}
	private int getLeftFromColumn(int column) {
		return column * mSqWidth;
	}
	
	private int getLeftFromId(int id) {
		return getLeftFromColumn(id % NUM_DAYS_PER_WEEK);
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		this.mWidth = MeasureSpec.getSize(widthMeasureSpec);
		this.mHeight = MeasureSpec.getSize(heightMeasureSpec);
		
		this.mSqWidth = mWidth / NUM_DAYS_PER_WEEK;
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

}
