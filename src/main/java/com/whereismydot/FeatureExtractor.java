/**
 * 
 */
package com.whereismydot;

import java.util.Map;

import twitter4j.Status;

/**
 * @author andrey
 * 
 * Interface for objects that given a tweet parsed by twitter4j produces a sparse feature vector 
 * represented by a map.  
 */
public interface FeatureExtractor {

	/**
	 * 
	 * @param tweet
	 * @return Features present in the tweet.
	 */
	public Map<String,Double> extract(Status tweet);
	
}
