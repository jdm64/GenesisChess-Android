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

	public String stdFormat()
	{
		Calendar cal = new GregorianCalendar();

		cal.setTime(this);

		String week = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US),
			month = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US),
			day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH)),
			year = String.valueOf(cal.get(Calendar.YEAR)),
			min = String.valueOf(cal.get(Calendar.MINUTE)),
			sec = String.valueOf(cal.get(Calendar.SECOND)),
			ampm = cal.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.US);

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

		long diff = Math.abs(now.getTime() - getTime());
		int count = 0;

		// months
		if (diff >= 2629743830L) {
			buff.append(String.valueOf(diff / 2629743830L) + " months ");
			diff %= 2629743830L;
			count++;
		}
		// weeks
		if (diff >= 604800000) {
			buff.append(String.valueOf(diff / 604800000) + " weeks ");
			diff %= 604800000;
			count++;

			if (count >= 2)
				return buff.toString() + "ago";
		}
		// days
		if (diff >= 86400000) {
			buff.append(String.valueOf(diff / 86400000) + " days ");
			diff %= 86400000;
			count++;

			if (count >= 2)
				return buff.toString() + "ago";
		}
		// hours
		if (diff >= 3600000) {
			buff.append(String.valueOf(diff / 3600000) + " hours ");
			diff %= 3600000;
			count++;

			if (count >= 2)
				return buff.toString() + "ago";
		}
		// minutes
		if (diff >= 60000) {
			buff.append(String.valueOf(diff / 60000) + " minutes ");
			diff %= 60000;
			count++;

			if (count >= 2)
				return buff.toString() + "ago";
		}
		// seconds
		buff.append(String.valueOf(diff / 1000) + " seconds ");
		return buff.toString() + "ago";
	}
}
