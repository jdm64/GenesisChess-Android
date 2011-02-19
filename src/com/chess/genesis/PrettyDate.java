package com.chess.genesis;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

class PrettyDate extends Date
{
	public PrettyDate(long milliseconds)
	{
		super(milliseconds);
	}

	public PrettyDate(String milliseconds)
	{
		super(Long.valueOf(milliseconds).longValue());
	}

	// !SICK!
	// This only exists because Calendar.getDisplayName was added
	// in API Level 9
	private String dayOfWeekToString(int day)
	{
		switch (day) {
		case Calendar.SUNDAY:
			return "Sun";
		case Calendar.MONDAY:
			return "Mon";
		case Calendar.TUESDAY:
			return "Tue";
		case Calendar.WEDNESDAY:
			return "Wed";
		case Calendar.THURSDAY:
			return "Thu";
		case Calendar.FRIDAY:
			return "Fri";
		case Calendar.SATURDAY:
			return "Sat";
		default:
			return "Err" + String.valueOf(day);
		}
	}

	// !SICK!
	// This only exists because Calendar.getDisplayName was added
	// in API Level 9
	private String monthToString(int month)
	{
		switch (month) {
		case Calendar.JANUARY:
			return "Jan";
		case Calendar.FEBRUARY:
			return "Feb";
		case Calendar.MARCH:
			return "Mar";
		case Calendar.APRIL:
			return "Apr";
		case Calendar.MAY:
			return "May";
		case Calendar.JUNE:
			return "Jun";
		case Calendar.JULY:
			return "Jul";
		case Calendar.AUGUST:
			return "Aug";
		case Calendar.SEPTEMBER:
			return "Sep";
		case Calendar.OCTOBER:
			return "Oct";
		case Calendar.NOVEMBER:
			return "Nov";
		case Calendar.DECEMBER:
			return "Dec";
		default:
			return "Err" + String.valueOf(month);
		}
	}

	// !SICK!
	// This only exists because Calendar.getDisplayName was added
	// in API Level 9
	private String ampmToString(int ampm)
	{
		switch (ampm) {
		case Calendar.AM:
			return "AM";
		case Calendar.PM:
			return "PM";
		default:
			return "Err" + String.valueOf(ampm);
		}
	}

	public String stdFormat()
	{
		Calendar cal = new GregorianCalendar();

		cal.setTime(this);

		String week = dayOfWeekToString(cal.get(Calendar.DAY_OF_WEEK)),
			month = monthToString(cal.get(Calendar.MONTH)),
			day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH)),
			year = String.valueOf(cal.get(Calendar.YEAR)),
			min = String.valueOf(cal.get(Calendar.MINUTE)),
			sec = String.valueOf(cal.get(Calendar.SECOND)),
			ampm = ampmToString(cal.get(Calendar.AM_PM));

		int hr_i = cal.get(Calendar.HOUR);
		if (hr_i == 0)
			hr_i = 12;
		String hr = String.valueOf(hr_i);

		return week + ", " + month + " " + day + ", " + year + " @ " + hr + ":" + min + ":" + sec + " " + ampm;
	}

	public String agoFormat()
	{
		StringBuffer buff = new StringBuffer();
		Date now = new Date();

		long tmp, diff = Math.abs(now.getTime() - getTime());
		int count = 0;

		// months
		if (diff >= 2629743830L) {
			tmp = diff / 2629743830L;
			if (tmp > 1)
				buff.append(String.valueOf(tmp) + " months ");
			else
				buff.append(String.valueOf(tmp) + " month ");
			diff %= 2629743830L;
			count++;
		}
		// weeks
		if (diff >= 604800000) {
			tmp = diff / 604800000;
			if (tmp > 1)
				buff.append(String.valueOf(tmp) + " weeks ");
			else
				buff.append(String.valueOf(tmp) + " week ");
			diff %= 604800000;
			count++;

			if (count >= 2)
				return buff.toString() + "ago";
		}
		// days
		if (diff >= 86400000) {
			tmp = diff / 86400000;
			if (tmp > 1)
				buff.append(String.valueOf(tmp) + " days ");
			else
				buff.append(String.valueOf(tmp) + " day ");
			diff %= 86400000;
			count++;

			if (count >= 2)
				return buff.toString() + "ago";
		}
		// hours
		if (diff >= 3600000) {
			tmp = diff / 3600000;
			if (tmp > 1)
				buff.append(String.valueOf(tmp) + " hours ");
			else
				buff.append(String.valueOf(tmp) + " hour ");
			diff %= 3600000;
			count++;

			if (count >= 2)
				return buff.toString() + "ago";
		}
		// minutes
		if (diff >= 60000) {
			tmp = diff / 60000;
			if (tmp > 1)
				buff.append(String.valueOf(tmp) + " minutes ");
			else
				buff.append(String.valueOf(tmp) + " minute ");
			diff %= 60000;
			count++;

			if (count >= 2)
				return buff.toString() + "ago";
		}
		// seconds
		tmp = diff / 1000;
		if (tmp > 1)
			buff.append(String.valueOf(tmp) + " seconds ");
		else
			buff.append(String.valueOf(tmp) + " second ");
		return buff.toString() + "ago";
	}
}
