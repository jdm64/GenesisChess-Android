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
}
