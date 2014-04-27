package com.whereismydot.utils;

import java.util.Date;

import org.joda.time.Days;
import org.joda.time.LocalDate;

public class TimeBinner {

	private static final LocalDate epoch = new LocalDate(2014,1,1);
	
	public long timeBin(Date date){
		LocalDate niceDate = new LocalDate(date);
		int day = Days.daysBetween(epoch, niceDate).getDays();
		
		if(day < 1){ 
			return 0;
		}else{
			return day;
		}
	}
}
