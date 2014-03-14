/**
 * 
 */
package com.whereismydot;

import twitter4j.Status;

/**
 * @author andrey
 *
 * Interface for objects used to prune out irrelevant tweets from the data set.
 */
public interface TweetFilter {

	/**
	 * 
	 * @param tweet
	 * @return True if the tweet is relevant and should be kept for processing further down the pipeline. 
	 */
	public boolean isRelevant(Status tweet);
}
