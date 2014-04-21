package com.whereismydot.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CompanyTimeBinner {
    static TimeBinner timeBinner = new TimeBinner();

	public long bin(Date date, long companyKey){
        long timeBin = timeBinner.timeBin(date);
	    return timeBin + companyKey;
	}
}
