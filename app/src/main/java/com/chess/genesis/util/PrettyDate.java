/* GenesisChess, an Android chess application
 * Copyright 2014, Justin Madru (justin.jdm64@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chess.genesis.util;

import java.util.*;

@SuppressWarnings("serial")
public class PrettyDate extends Date
{
	private final static long MONTHS = 2629743830L;
	private final static long WEEKS = 604800000;
	private final static long DAYS = 86400000;
	private final static long HOURS = 3600000;
	private final static long MINUTES = 60000;
	private final static long SECONDS = 1000;

	public PrettyDate()
	{
	}

	public PrettyDate(final long milliseconds)
	{
		super(milliseconds);
	}

	public PrettyDate(final String milliseconds)
	{
		super(Long.parseLong(milliseconds));
	}

	// !SICK!
	// This only exists because Calendar.getDisplayName was added
	// in API Level 9
	private static String dayOfWeekToString(final int day)
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
			return "Err" + day;
		}
	}

	// !SICK!
	// This only exists because Calendar.getDisplayName was added
	// in API Level 9
	private static String monthToString(final int month)
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
			return "Err" + month;
		}
	}

	// !SICK!
	// This only exists because Calendar.getDisplayName was added
	// in API Level 9
	private static String ampmToString(final int ampm)
	{
		switch (ampm) {
		case Calendar.AM:
			return "AM";
		case Calendar.PM:
			return "PM";
		default:
			return "Err" + ampm;
		}
	}

	public String stdFormat()
	{
		final Calendar cal = new GregorianCalendar();

		cal.setTime(this);

		final String week = dayOfWeekToString(cal.get(Calendar.DAY_OF_WEEK)),
			month = monthToString(cal.get(Calendar.MONTH)),
			day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH)),
			year = String.valueOf(cal.get(Calendar.YEAR)),
			min = String.valueOf(cal.get(Calendar.MINUTE)),
			sec = String.valueOf(cal.get(Calendar.SECOND)),
			ampm = ampmToString(cal.get(Calendar.AM_PM));

		int hr_i = cal.get(Calendar.HOUR);
		if (hr_i == 0)
			hr_i = 12;
		final String hr = String.valueOf(hr_i);

		return week + ", " + month + ' ' + day + ", " + year + " @ " + hr + ':' + min + ':' + sec + ' ' + ampm;
	}

	public String dayFormat()
	{
		final Calendar cal = new GregorianCalendar();

		cal.setTime(this);

		final String year = String.valueOf(cal.get(Calendar.YEAR)),
			month = monthToString(cal.get(Calendar.MONTH)),
			day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

		return year + '-' + month + '-' + day;
	}

	public String agoFormat()
	{
		final Date now = new Date();

		long diff = Math.abs(now.getTime() - getTime());
		int count = 0;

		List<String> parts = new ArrayList<>();

		// months
		if (diff >= MONTHS) {
			parts.add((diff / MONTHS) + "m");
			diff %= MONTHS;
			count++;
		}
		// weeks
		if (diff >= WEEKS) {
			parts.add((diff / WEEKS) + "w");
			diff %= WEEKS;
			count++;

			if (count == 2)
				return String.join(" ", parts);
		}
		// days
		if (diff >= DAYS) {
			parts.add((diff / DAYS) + "d");
			diff %= DAYS;
			count++;

			if (count == 2)
				return String.join(" ", parts);
		}
		// hours
		if (diff >= HOURS) {
			parts.add((diff / HOURS) + "h");
			diff %= HOURS;
			count++;

			if (count == 2)
				return String.join(" ", parts);
		}
		// minutes
		if (diff >= MINUTES) {
			parts.add((diff / MINUTES) + "m");
			diff %= MINUTES;
			count++;

			if (count == 2)
				return String.join(" ", parts);
		}
		// seconds
		if (diff >= SECONDS) {
			parts.add((diff / SECONDS) + "s");
			count++;
		}

		return (count >= 1)? String.join(" ", parts) : "now";
	}
}
