package com.mmscn.titlebar;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;

public class Notification {
	public final static int NOTIFICATION_INFO = 0;
	public final static int NOTIFICATION_WARNING = 1;
	public final static int NOTIFICATION_ERROR = 2;

	public final static int MAX_NUM_NOTIFICATION_ITEM = 8;

	private final static int INFO_LIFE = 1000;
	private final static int WARNING_LIFE = 2000;
	private final static int ERROR_LIFE = 4000;

	
	private static NotificationHandler mHandler;
	

	public static int getLife(int type) {
		switch (type) {
		case NOTIFICATION_INFO:
			return INFO_LIFE;
		case NOTIFICATION_WARNING:
			return WARNING_LIFE;
		case NOTIFICATION_ERROR:
			return ERROR_LIFE;
		}
		return 1;
	}

	public static void bindNotificationTitleBar(NotificationTitleBar titleBar) {
		if(mHandler == null) {
			mHandler = new NotificationHandler();
		}
		mHandler.BindTitleBar(titleBar);
	}

	public static void i(String title) {
		if(mHandler != null) {
			mHandler.obtainMessage(NOTIFICATION_INFO, title).sendToTarget();
		}
	}

	public static void w(String title) {
		if(mHandler != null) {
			mHandler.obtainMessage(NOTIFICATION_WARNING, title).sendToTarget();
		}
	}

	public static void e(String title) {
		if(mHandler != null) {
			mHandler.obtainMessage(NOTIFICATION_ERROR, title).sendToTarget();
		}
	}

	public static class NotificationItem {
		public int type;
		public String title;

		public NotificationItem(int type, String title) {
			this.type = type;
			this.title = title;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof NotificationItem) {
				return title.contentEquals(((NotificationItem) o).title);
			}
			return super.equals(o);
		}
	}
	
	private static class NotificationHandler extends Handler {
		private WeakReference<NotificationTitleBar> mTitleBar;
		private void BindTitleBar(NotificationTitleBar titleBar) {
			this.mTitleBar = new WeakReference<NotificationTitleBar>(titleBar);
		}

		@Override
		public void handleMessage(Message msg) {
			if(mTitleBar == null) 
				return;
			NotificationTitleBar titleBar = this.mTitleBar.get();
			if(titleBar == null) return;
			if(!(msg.obj instanceof String)) return;
			String title = (String) msg.obj;
			switch(msg.what) {
			case NOTIFICATION_INFO:
				titleBar.newNotification(new NotificationItem(NOTIFICATION_INFO,
						title));
				break;
			case NOTIFICATION_WARNING:
				titleBar.newNotification(new NotificationItem(
						NOTIFICATION_WARNING, title));
				break;
			case NOTIFICATION_ERROR:
				titleBar.newNotification(new NotificationItem(NOTIFICATION_ERROR,
						title));
				break;
			}
		}
	}
}
