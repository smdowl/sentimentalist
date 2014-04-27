package com.whereismydot.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeBinner {

	public long timeBin(Date date){
		
		Calendar cal = new GregorianCalendar();
	    cal.setTime(date);
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

	    return cal.getTime().getTime();
	}
}
