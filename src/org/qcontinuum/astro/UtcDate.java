// J2ME Compass
// Copyright (C) 2006 Dana Peters
// http://www.qcontinuum.org/compass

package org.qcontinuum.astro;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class UtcDate {

	private int mYear, mMonth, mDay, mHour, mMinute, mSecond;

	public UtcDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getDefault());
		cal.setTime(date);
		mYear = cal.get(Calendar.YEAR);
		mMonth = cal.get(Calendar.MONTH) + 1;
		mDay = cal.get(Calendar.DAY_OF_MONTH);
		mHour = cal.get(Calendar.HOUR_OF_DAY);
		mMinute = cal.get(Calendar.MINUTE);
		mSecond = cal.get(Calendar.SECOND);
	}

	public UtcDate(int year, int month, int day, int hour, int minute, int second) {
		mYear = year;
		mMonth = month;
		mDay = day;
		mHour = hour;
		mMinute = minute;
		mSecond = second;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(mYear);
		sb.append('-');
		sb.append(mMonth);
		sb.append('-');
		sb.append(mDay);
		sb.append(' ');
		sb.append(mHour);
		sb.append(':');
		if (mMinute < 10) {
			sb.append('0');
		}
		sb.append(mMinute);
		sb.append(':');
		if (mSecond < 10) {
			sb.append('0');
		}
		sb.append(mSecond);
		return sb.toString();
	}

	public String getDateString() {
		StringBuffer sb = new StringBuffer();
		sb.append(mYear);
		sb.append('-');
		sb.append(mMonth);
		sb.append('-');
		sb.append(mDay);
		return sb.toString();
	}

	public String getTimeString() {
		StringBuffer sb = new StringBuffer();
		sb.append(mHour);
		sb.append(':');
		if (mMinute < 10) {
			sb.append('0');
		}
		sb.append(mMinute);
		sb.append(':');
		if (mSecond < 10) {
			sb.append('0');
		}
		sb.append(mSecond);
		return sb.toString();
	}
}
