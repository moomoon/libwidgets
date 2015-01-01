package com.mmscn.titlebar;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Scroller;

import com.mmscn.titlebar.Notification.NotificationItem;
import com.mmscn.widgets.R;

public class NotificationTitleBar extends ProgressTitleBar {
	private Paint mPtBgI;
	private Paint mPtBgW;
	private Paint mPtBgE;
	private Paint mPtLine;
	private Paint mPtText;
	private int mNotificationHeight;
	private int mNotificationTop;
	private List<NotificationItem> mList;
	private Scroller mScroller;
	private Handler mHandler;
	private int i;
	private Rect r;
	private NotificationItem item;
	private float top, bottom;

	public NotificationTitleBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		Resources res = context.getResources();
		int colorI = res.getColor(R.color.notification_info);
		int colorW = res.getColor(R.color.notification_warning);
		int colorE = res.getColor(R.color.notification_error);
		int colorLine = res.getColor(R.color.notification_seperator);

		float textSize = res.getDimension(R.dimen.notification_text);

		mNotificationHeight = (int) res.getDimension(R.dimen.notification_item_height);

		mPtBgI = new Paint();
		mPtBgW = new Paint();
		mPtBgE = new Paint();
		mPtText = new Paint();
		mPtLine = new Paint();

		mPtBgI.setColor(colorI);
		mPtBgW.setColor(colorW);
		mPtBgE.setColor(colorE);
		mPtLine.setColor(colorLine);
		// mPtLine.setColor(Color.BLACK);

		mPtText.setColor(Color.BLACK);
		mPtText.setTextSize(textSize);
		mPtText.setTextAlign(Align.CENTER);
		mPtText.setAntiAlias(true);

		r = new Rect();
		mScroller = new Scroller(context);
		mHandler = new Handler();
	}

	private float getNotificationTop(int id) {
		return mNotificationTop + id * mNotificationHeight;
	}

	@Override
	protected void drawTitleBar(Canvas canvas) {
		drawNotifications(canvas);
		super.drawTitleBar(canvas);
	}

	protected void drawNotifications(Canvas canvas) {
		if (mList != null && mList.size() > 0) {
			canvas.save();
			canvas.clipRect(0, getPaddingTop(), mWidth, mHeight - getPaddingBottom());
			for (i = 0; i < mList.size(); i++) {
				item = mList.get(i);
				top = getNotificationTop(i);
				bottom = top + mNotificationHeight;
				canvas.drawRect(0f, top, mWidth, bottom, getPaint(item.type));
				mPtText.getTextBounds(item.title, 0, item.title.length(), r);
				canvas.drawText(item.title, mWidth / 2, getNotificationTop(i) + mNotificationHeight / 2 + r.height()
						/ 2, mPtText);
				bottom--;
				canvas.drawLine(0, bottom, mWidth, bottom, mPtLine);
				Log.i("notification", "draw notif = " + i);
			}
			canvas.restore();
		}
	}

	private Paint getPaint(int type) {
		switch (type) {
		case Notification.NOTIFICATION_ERROR:
			return mPtBgE;
		case Notification.NOTIFICATION_WARNING:
			return mPtBgW;
		}
		return mPtBgI;
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			this.mNotificationTop = mScroller.getCurrY();
			invalidate();
		}
	}

	public void newNotification(NotificationItem item) {
		if (mList == null) {
			mList = new ArrayList<NotificationItem>();
		}
		mList.add(0, item);
		mScroller.abortAnimation();
		mScroller.startScroll(0, (int) getTitleBarBottom() - mNotificationHeight, 0, mNotificationHeight);
		invalidate();
		mHandler.postDelayed(new ItemRemover(item), Notification.getLife(item.type));
		while (mList.size() > Notification.MAX_NUM_NOTIFICATION_ITEM) {
			mList.remove(Notification.MAX_NUM_NOTIFICATION_ITEM);
		}
		Log.i("notification", "new notif = " + mList.size());

	}

	private class ItemRemover implements Runnable {
		private NotificationItem item;

		private ItemRemover(NotificationItem item) {
			this.item = item;
		}

		@Override
		public void run() {
			Log.i("remove", "remove");
			if (item != null && mList != null) {
				Log.i("remove", "valid");
				mList.remove(item);
				invalidate();
			}
			Log.i("remove", "invalid");
		}
	}

}
