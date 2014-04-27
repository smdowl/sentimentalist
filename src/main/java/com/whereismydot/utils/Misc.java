package com.whereismydot.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.nativeio.NativeIO.POSIX.Stat;

import twitter4j.Status;

public class Misc {
	static public <T> List<T> getBatch(Iterator<T> iter, int count){
		List<T> result = new ArrayList<T>();
	    while(iter.hasNext() && result.size() < count){
	    	result.add(iter.next());
	    }
	    	
	    return result;
	 }
	
	public static List<Status> getBatchTweets(Iterator<Text> iter, int count){
		List<Status> result = new ArrayList<Status>();
		for(Text text : getBatch(iter, count)){
			Status status =  TwitterParser.parseOrNull(text.toString());
    		if(status != null){
    			result.add(status);
    		}
		}		
		return result;
	}
}
