package com.mmscn.widgets;

import java.text.DecimalFormat;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.mmscn.widgets.GraphGestureDetector.OnGestureListener;

public class GraphView extends View implements OnGestureListener {
	private final static float MAX_VALUE_POSITION = 0.7f;
	private final static float MIN_VALUE_POSITION = 0.2f;
	protected final static float MAX_TIME_POSITION = 0.9f;
	protected final static float MIN_TIME_POSITION = 0.15f;

	private final static float MAX_SCALE_FACTOR = 10f;
	private final static float MIN_SCALE_FACTOR = 0.1f;

	private final static int MIN_VALUE_DIVISION = 5;

	private final static int MIN_TIME_DIVISION = 2;
	private final static long MINUTE = 60 * 1000;
	private final static long TWO_MINUTE = 2 * MINUTE;
	private final static long FIVE_MINUTE = 5 * MINUTE;
	private final static long QUARTER_HOUR = 15 * MINUTE;
	private final static long HALF_HOUR = QUARTER_HOUR * 2;
	private final static long HOUR = QUARTER_HOUR * 4;
	private final static long THREE_HOUR = HOUR * 3;
	private final static long SIX_HOUR = THREE_HOUR * 2;
	private final static long HALF_DAY = SIX_HOUR * 2;
	private final static long DAY = HALF_DAY * 2;
	private final static long TWO_DAY = DAY * 2;
	private final static long FIVE_DAY = DAY * 5;
	private final static long HALF_MONTH = DAY * 15;
	private final static long MONTH = HALF_MONTH * 2;
	private final static long SEASON = MONTH * 3;
	private final static long HALF_YEAR = MONTH * 6;
	private final static long YEAR = HALF_YEAR * 2;
	private final static long TWO_YEAR = YEAR * 2;
	private final static long FIVE_YEAR = YEAR * 5;
	private final static long TEN_YEAR = YEAR * 10;
	private final static long[] TIME_DIVISION = new long[] { TEN_YEAR, FIVE_YEAR, TWO_YEAR, YEAR, HALF_YEAR, SEASON,
			MONTH, HALF_MONTH, FIVE_DAY, TWO_DAY, DAY, HALF_DAY, SIX_HOUR, THREE_HOUR, HOUR, HALF_HOUR, QUARTER_HOUR,
			FIVE_MINUTE, TWO_MINUTE, MINUTE };
	private int mTimeUnit;
	private long mTimeStart;

	private final static float SELECTOR_POSITION = 0.8f;

	protected final static int PADDING = 20;
	protected final static int RADIUS = 10;
	protected final static int RADIUS_CURRENT = 20;

	private final static int SELECTOR_WIDTH = 250;
	private final static int SELECTOR_HEIGHT = 50;

	private String unit;

	protected int mHeight;
	protected int mWidth;
	private float mScaleFactorX;
	private float mScaleAnchorX;
	private float mOffsetX;
	private float mTimeToPointRatio;
	private float mValueToPointRatio = 1;
	protected int mNumPoints;

	private float mMinValue;
	private float mValueRange;
	protected float[] mValues;
	protected long[] mTimePoints;

	private int mSelectedId = -1;

	protected int mCurrentId = -1;

	private float mOrdUnit;

	private GraphGestureDetector mDetector;

	private boolean isUnitInteger;

	protected Paint mPtGraph;
	private Paint mPtLabelText;
	private Paint mPtAxis;
	private Paint mPtSelector;
	private Paint mPtUnit;
	private Paint mPtGrid;

	public GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {

		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GraphView);
		mDetector = new GraphGestureDetector(context, this);
		mPtGraph = new Paint();
		mPtAxis = new Paint();
		mPtLabelText = new Paint();
		mPtSelector = new Paint();
		mPtUnit = new Paint();
		mPtGrid = new Paint();
		mPtGraph.setColor(ta.getColor(R.styleable.GraphView_graphColor, Color.BLACK));
		mPtAxis.setColor(ta.getColor(R.styleable.GraphView_axisColor, Color.BLACK));
		mPtLabelText.setColor(ta.getColor(R.styleable.GraphView_labelColor, Color.BLACK));
		mPtSelector.setColor(ta.getColor(R.styleable.GraphView_selectorColor, Color.BLACK));
		mPtUnit.setColor(ta.getColor(R.styleable.GraphView_unitColor, Color.BLACK));
		mPtGrid.setColor(ta.getColor(R.styleable.GraphView_gridColor, Color.BLACK));
		mPtUnit.setTextAlign(Align.LEFT);
		mPtUnit.setFakeBoldText(true);
		mPtUnit.setTextSize(ta.getDimension(R.styleable.GraphView_unitTextSize, 30f));
		mPtLabelText.setTextSize(ta.getDimension(R.styleable.GraphView_labelTextSize, 20f));

		mPtSelector.setTextSize(ta.getDimension(R.styleable.GraphView_selectorTextSize, 20f));
		mPtGraph.setStrokeWidth(ta.getInt(R.styleable.GraphView_graphStrokeWidth, 3));
		mPtLabelText.setTextAlign(Align.RIGHT);
		mPtGraph.setStyle(Style.STROKE);
		mPtGraph.setAntiAlias(true);
		mPtGrid.setAlpha((int) ta.getFraction(R.styleable.GraphView_gridAlpha, 40, 256, 40));
		ta.recycle();
	}

	public void setData(float[] values, long[] timePoints) {
		mScaleFactorX = 1;
		mOffsetX = 0f;
		this.mValues = values;
		this.mTimePoints = timePoints;
		this.mNumPoints = Math.min(values.length, timePoints.length);

		for (int i = 0; i < mNumPoints; i++) {
			Log.i("setdata", "value = " + mValues[i] + " time = " + TimeUtils.toDateTime(mTimePoints[i]));
		}
		updateValueRange();
		updateTimeToPointRatio();
		updateValueToPointRatio();
		updateOrdinateUnit();
		updateTimeUnit();
		updateTimeStart();
		// updateDisplayRange();
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	private void updateValueRange() {
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		for (float f : mValues) {
			if (f < min) {
				min = f;
			}
			if (f > max) {
				max = f;
			}
		}
		mMinValue = min;
		mValueRange = max - min;
	}

	private void updateOrdinateUnit() {
		int count = 0;
		float range = Math.abs(mValueRange);
		while (range > 1) {
			count++;
			range /= 10;
		}
		while (range < 1) {
			count--;
			range *= 10;
		}
		mOrdUnit = (float) Math.pow(10, count);
		if (Math.abs(mValueRange) / mOrdUnit <= MIN_VALUE_DIVISION / 2)
			mOrdUnit /= 2;
		isUnitInteger = mOrdUnit == (int) mOrdUnit;
	}

	// private void updateDisplayRange() {
	// // TODO
	// }

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mNumPoints > 0) {
			drawGraph(canvas);
			drawAxis(canvas);
		}

		// canvas.drawLine(100, 200, 200,200, mPtGraphNormal);
		// canvas.save();
		// canvas.translate(100, 0);
		// canvas.scale(2, 1, 150, 0);
		//
		// canvas.drawLine(100, 300, 200,300, mPtGraphNormal);
		// canvas.restore();
		//
		// canvas.drawLine(350, 0, 350,300, mPtGraphNormal);

	}

	protected void drawGraph(Canvas canvas) {
		canvas.save();
		canvas.clipRect(MIN_TIME_POSITION * mWidth - PADDING, PADDING, MAX_TIME_POSITION * mWidth + PADDING, mHeight
				- PADDING);

		for (int i = 0; i < mNumPoints - 1; i++) {
			canvas.drawLine(getScaledPoint(getXPosFromTime(mTimePoints[i])), getYPosFromValue(mValues[i]),
					getScaledPoint(getXPosFromTime(mTimePoints[i + 1])), getYPosFromValue(mValues[i + 1]), mPtGraph);
		}
		for (int i = 0; i < mNumPoints; i++) {
			canvas.drawCircle(getScaledPoint(getXPosFromTime(mTimePoints[i])), getYPosFromValue(mValues[i]),
					i == mCurrentId ? RADIUS_CURRENT : RADIUS, mPtGraph);
		}
		drawSelection(canvas);
		canvas.restore();

	}

	private void drawAxis(Canvas canvas) {
		float graphLeft = MIN_TIME_POSITION * mWidth - PADDING;
		float graphRight = MAX_TIME_POSITION * mWidth + PADDING;
		canvas.drawLine(graphLeft, 0, graphLeft, mHeight - PADDING, mPtAxis);
		float pos;
		String s;
		mPtLabelText.setTextAlign(Align.RIGHT);
		mPtLabelText.setTextScaleX(1);
		for (float ord = (int) Math.ceil((getValueFromYPos(mHeight - PADDING)) / mOrdUnit) * mOrdUnit; (pos = getYPosFromValue(ord)) > 0; ord += mOrdUnit) {
			if (isUnitInteger)
				s = String.valueOf((int) ord);
			else {
				s = new DecimalFormat("#0.000").format(ord);
			}
			canvas.drawLine(graphLeft, pos, graphRight, pos, mPtGrid);
			canvas.drawText(s
			// .substring(0, Math.min(s.length(), 4))
					, MIN_TIME_POSITION * mWidth - PADDING - 5, pos, mPtLabelText);
		}
		if (unit != null)
			canvas.drawText(unit, graphLeft + 10f, 80f, mPtUnit);

		canvas.drawLine(graphLeft, mHeight - PADDING, graphRight, mHeight - PADDING, mPtAxis);
		mPtLabelText.setTextAlign(Align.CENTER);
		long t = mTimeStart;
		// private final static long[] TIME_DIVISION = new long[] { TEN_YEAR,
		// FIVE_YEAR, TWO_YEAR, YEAR, HALF_YEAR, SEASON, MONTH, HALF_MONTH,
		// FIVE_DAY, TWO_DAY, DAY, HALF_DAY, SIX_HOUR, THREE_HOUR, HOUR,
		// HALF_HOUR, QUARTER_HOUR, FIVE_MINUTE, TWO_MINUTE, MINUTE };
		for (; (pos = getScaledPoint(getXPosFromTime(t))) < graphRight; t += TIME_DIVISION[mTimeUnit]) {
			if (pos >= graphLeft)
				canvas.drawLine(pos, 0, pos, mHeight - PADDING, mPtGrid);
			if (mTimeUnit <= 3) {
				s = TimeUtils.toYear(t);
			} else if (mTimeUnit <= 6) {
				s = TimeUtils.toYearMonth(t);
			} else if (mTimeUnit <= 10) {
				mPtLabelText.setTextScaleX(0.85f);
				s = TimeUtils.toDate(t);
			} else {
				mPtLabelText.setTextScaleX(0.6f);
				s = TimeUtils.toDateTime(t);
			}
			canvas.drawText(s, pos, mHeight - PADDING + mPtLabelText.getTextSize(), mPtLabelText);
		}
	}

	protected void drawSelection(Canvas canvas) {
		if (mSelectedId < 0 || mSelectedId >= mNumPoints) {
			return;
		}
		float left = getScaledPoint(getXPosFromId(mSelectedId));
		float top = getYPosFromId(mSelectedId);

		float[] lines = new float[12];
		int i = 0;
		lines[i++] = left;
		lines[i++] = top;
		lines[i++] = left;
		lines[i++] = SELECTOR_POSITION * mHeight;
		lines[i++] = left;
		lines[i++] = SELECTOR_POSITION * mHeight;
		lines[i++] = left + SELECTOR_HEIGHT;
		lines[i++] = SELECTOR_POSITION * mHeight + SELECTOR_HEIGHT;
		lines[i++] = left + SELECTOR_HEIGHT;
		lines[i++] = SELECTOR_POSITION * mHeight + SELECTOR_HEIGHT;
		lines[i++] = left + SELECTOR_HEIGHT + SELECTOR_WIDTH;
		lines[i++] = SELECTOR_POSITION * mHeight + SELECTOR_HEIGHT;
		canvas.drawLines(lines, mPtSelector);
		canvas.drawText(String.format("%s %s",
		// mValues[mSelectedId] == (int) mValues[mSelectedId] ? String
		// .valueOf((int) mValues[mSelectedId]):
				new DecimalFormat("#0.000").format(mValues[mSelectedId]), unit == null ? "" : unit), left
				+ SELECTOR_HEIGHT, SELECTOR_POSITION * mHeight + SELECTOR_HEIGHT - 5, mPtSelector);
		canvas.drawText(TimeUtils.toDateTime(mTimePoints[mSelectedId]), left + SELECTOR_HEIGHT, SELECTOR_POSITION
				* mHeight + SELECTOR_HEIGHT + 25, mPtSelector);
	}

	private float getXPosFromId(int id) {
		return getXPosFromTime(mTimePoints[id]);
	}

	protected float getXPosFromTime(long t) {
		return (t - mTimePoints[0]) * mTimeToPointRatio + mWidth * MIN_TIME_POSITION;
	}

	private long getTimeFromXPos(float x) {
		return (long) ((x - mWidth * MIN_TIME_POSITION) / mTimeToPointRatio) + mTimePoints[0];
	}

	private float getYPosFromId(int id) {
		return getYPosFromValue(mValues[id]);
	}

	protected float getYPosFromValue(float v) {
		return MAX_VALUE_POSITION * mHeight - (v - mMinValue) * mValueToPointRatio;
	}

	private float getValueFromYPos(float y) {
		return (MAX_VALUE_POSITION * mHeight - y) / mValueToPointRatio + mMinValue;
	}

	private long totalTimeSpan() {
		if (mTimePoints != null)
			return mTimePoints[mNumPoints - 1] - mTimePoints[0];
		return 1L;
	}

	private void updateTimeToPointRatio() {
		mTimeToPointRatio = scrollableWidth() / totalTimeSpan();
	}

	private void updateValueToPointRatio() {
		mValueToPointRatio = mHeight * (MAX_VALUE_POSITION - MIN_VALUE_POSITION) / mValueRange;
		// TODO
	}

	public void setCurrent(long current) {
		int id = 0;
		while (id < mNumPoints && current > mTimePoints[id]) {
			id++;
		}
		mCurrentId = id;
		Log.i("setCurrent",
				"current = " + TimeUtils.toDateTime(current) + " set to "
						+ TimeUtils.toDateTime(mTimePoints[mCurrentId]));
		mSelectedId = mCurrentId;
		invalidate();
	}

	private float scrollableWidth() {
		return mWidth * (MAX_TIME_POSITION - MIN_TIME_POSITION);
	}

	private void updateTimeUnit() {
		long division = (long) ((mTimePoints[mNumPoints - 1] - mTimePoints[0]) / mScaleFactorX / MIN_TIME_DIVISION);
		Log.i("division", "division = " + division);
		int i;
		for (i = 0; i < TIME_DIVISION.length - 1; i++) {
			if (division >= TIME_DIVISION[i])
				break;
		}
		mTimeUnit = i;

		Log.i("division", "mTimeUnit = " + mTimeUnit);
	}

	private void updateTimeStart() {
		Time t = new Time(Time.getCurrentTimezone());
		t.set(getTimeFromXPos(getActualPoint(MIN_VALUE_POSITION * mWidth)));
		switch (mTimeUnit) {
		case 3:
			t.month = 0;
		case 6:
			t.monthDay = 0;
		case 10:
			t.hour = 0;
		case 14:
			t.minute = 0;
		case 19:
			t.second = 0;
			break;
		case 0:
			divideCentury(t, 10);
			break;
		case 1:
			divideCentury(t, 5);
			break;
		case 2:
			divideCentury(t, 2);
			break;
		case 4:
			divideYear(t, 6);
			break;
		case 5:
			divideYear(t, 3);
			break;
		case 7:
			divideMonth(t, 15);
			break;
		case 8:
			divideMonth(t, 5);
			break;
		case 9:
			divideMonth(t, 2);
			break;
		case 11:
			divideDay(t, 12);
			break;
		case 12:
			divideDay(t, 6);
			break;
		case 13:
			divideDay(t, 3);
			break;
		case 15:
			divideHour(t, 30);
			break;
		case 16:
			divideHour(t, 15);
			break;
		case 17:
			divideHour(t, 5);
			break;
		case 18:
			divideHour(t, 2);
			break;

		// private final static long[] TIME_DIVISION = new long[] { TEN_YEAR,
		// FIVE_YEAR, TWO_YEAR, YEAR, HALF_YEAR, SEASON, MONTH, HALF_MONTH,
		// FIVE_DAY, TWO_DAY, DAY, HALF_DAY, SIX_HOUR, THREE_HOUR, HOUR,
		// HALF_HOUR, QUARTER_HOUR, FIVE_MINUTE, TWO_MINUTE, MINUTE };
		}
		mTimeStart = t.toMillis(true);
	}

	private void divideCentury(Time t, int years) {
		t.year /= years;
		t.year *= years;
		t.month = 0;
		t.monthDay = 0;
		t.hour = 0;
		t.minute = 0;
		t.second = 0;
	}

	private void divideYear(Time t, int months) {
		t.month /= months;
		t.month *= months;
		t.monthDay = 0;
		t.hour = 0;
		t.minute = 0;
		t.second = 0;
	}

	private void divideMonth(Time t, int days) {
		t.monthDay /= days;
		t.monthDay *= days;
		t.hour = 0;
		t.minute = 0;
		t.second = 0;
	}

	private void divideDay(Time t, int hours) {
		t.hour /= hours;
		t.hour *= hours;
		t.minute = 0;
		t.second = 0;
	}

	private void divideHour(Time t, int minutes) {
		t.minute /= minutes;
		t.minute *= minutes;
		t.second = 0;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mDetector.onTouchEvent(event);
		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		this.mHeight = MeasureSpec.getSize(heightMeasureSpec);
		this.mWidth = MeasureSpec.getSize(widthMeasureSpec);
		updateTimeToPointRatio();
		updateValueToPointRatio();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public void onScroll(float dx) {
		mOffsetX -= dx;
		mOffsetX = Math.max(Math.min(scrollableWidth(), mOffsetX), -scrollableWidth());
		updateTimeStart();
		invalidate();
	}

	@Override
	public void onScale(float scaleFactorX) {
		this.mScaleFactorX *= scaleFactorX;
		this.mScaleFactorX = Math.max(Math.min(mScaleFactorX, MAX_SCALE_FACTOR), MIN_SCALE_FACTOR);
		updateTimeUnit();
		updateTimeStart();
		invalidate();
	}

	private float getActualPoint(float x) {
		return (x - mScaleAnchorX - mOffsetX) / mScaleFactorX + mScaleAnchorX;
	}

	protected float getScaledPoint(float x) {
		return (x - mScaleAnchorX) * mScaleFactorX + mScaleAnchorX + mOffsetX;
	}

	private int getNearestValuePoint(float x) {
		if (x <= getXPosFromId(0)) {
			return 0;
		}
		for (int i = 0; i < mNumPoints - 1; i++) {
			if (x < (getXPosFromId(i) + getXPosFromId(i + 1)) / 2) {
				return i;
			}
		}
		return mNumPoints - 1;
	}

	@Override
	public void onScaleStart(float x) {
		int id = getNearestValuePoint(getActualPoint(x));
		Log.i("idchk", "id = " + id);
		float scaleAnchor = getXPosFromId(id);
		mOffsetX -= (scaleAnchor - mScaleAnchorX) * (1 - mScaleFactorX);
		mScaleAnchorX = scaleAnchor;
	}

	@Override
	public void onSingleTapUp(float x, float y) {
		int id = getNearestValuePoint(getActualPoint(x));
		if (id == mSelectedId) {
			mSelectedId = -1;
		} else {
			mSelectedId = id;
		}
		invalidate();
	}

}
