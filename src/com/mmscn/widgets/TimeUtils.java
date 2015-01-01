package com.mmscn.widgets;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.text.format.Time;
import android.util.Log;

public class TimeUtils {
	public final static long YEAR = (long) (365.2425d * 24 * 3600 * 1000);

	public static boolean isTimeBefore(String lTime, String rTime) {
		String[] ls = lTime.split("\\:");
		String[] rs = rTime.split("\\:");
		int[] li = new int[] { Integer.parseInt(ls[0]), Integer.parseInt(ls[1]) };
		int[] ri = new int[] { Integer.parseInt(rs[0]), Integer.parseInt(rs[1]) };
		return (li[0] - ri[0]) * 60 + li[1] - ri[1] < 0;
	}

	public static boolean isTimeInOrder(String timePoint0, String timePoint1, String timePoint2) {
		boolean result = isTimeBefore(timePoint0, timePoint1) && isTimeBefore(timePoint1, timePoint2);
		Log.e("isTimeInOrder", timePoint0 + " " + timePoint1 + " " + timePoint2 + " " + result);
		return result;
	}

	public static boolean isTimeFittableInOneDay(String timePoint0, String timePoint1, String timePoint2) {
		return isTimeInOrder(timePoint0, timePoint1, timePoint2) || isTimeInOrder(timePoint2, timePoint0, timePoint1)
				|| isTimeInOrder(timePoint1, timePoint2, timePoint0);
	}

	public static String toYearMonth(long t) {
		Time time = getTime(t);
		return new StringBuilder().append(time.year).append('-').append(time.month + 1).toString();
	}

	public static String toMonth(long t) {
		return String.valueOf(getTime(t).month + 1);
	}

	public static String toYear(long t) {
		return String.valueOf(getTime(t).year);
	}

	public static String toDate(long t) {
		return toDate(getTime(t));
	}

	public static String toDate(Time t) {
		return t.format("%Y-%m-%d");
	}

	public static String toMonthDay(long t) {
		return new StringBuilder().append(getTime(t).monthDay).toString();
	}

	public static String toTime(long t) {
		return toTime(getTime(t));
	}

	public static String toTime(Time t) {
		return t.format("%H:%M");
	}

	public static String toDateTime(long t) {
		return getTime(t).format("%Y-%m-%d %H:%M");
	}

	public static String toDateTimeSecond(long t) {
		return getTime(t).format("%Y-%m-%d %H:%M:%S");
	}

	private static Time getTime(long t) {
		Time time = new Time(Time.getCurrentTimezone());
		time.set(t);
		return time;
	}

	public static long parseDateTimeSecond(String dateTimeSecond) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.CHINA);
		try {
			Date d = format.parse(dateTimeSecond);
			return d.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0L;
	}

	public static long parseDateTime(String dateTime) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm", Locale.CHINA);
		try {
			Date d = format.parse(dateTime);
			return d.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0L;
	}

}
