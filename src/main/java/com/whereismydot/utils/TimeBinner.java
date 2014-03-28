package com.whereismydot.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeBinner {

	public long timeBin(Date date){
		
		Calendar cal = new GregorianCalendar();
	    cal.setTime(date);

	    if (cal.get(Calendar.SECOND) >= 30){
	        cal.add(Calendar.MINUTE, 1);
	    }

	    cal.set(Calendar.SECOND, 0);

	    return cal.getTime().getTime();
	}
}
