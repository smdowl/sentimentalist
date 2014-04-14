package com.whereismydot.extractors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import twitter4j.Status;

/**
 * Class for collecting statistics about the users that have posted in the given tweets list.
 */
public class UserFeatures implements FeatureExtractor {

	@Override
	public Map<String, Double> extract(List<Status> tweets) {
		Map <String,Double> features = new HashMap<String, Double>();
        
		for (Status tweet : tweets){
			// this counts the total retweets for each user's posts in the given list of tweets:
			double retweets = features.containsKey("user (retweets): " + tweet.getUser().getId())?
					features.get("user (retweets): " + tweet.getUser().getId()) + tweet.getRetweetCount():
						tweet.getRetweetCount();
						
			features.put("user (retweets in bin): " + tweet.getUser().getId(), retweets);
			features.put("user (favourites): " + tweet.getUser().getId(), 
					(double)tweet.getUser().getFavouritesCount());
			features.put("user (followers): " + tweet.getUser().getId(), 
					(double)tweet.getUser().getFollowersCount());
			features.put("user (friends): " + tweet.getUser().getId(), 
					(double)tweet.getUser().getFriendsCount());
			features.put("user (listed count): " + tweet.getUser().getId(), 
					(double)tweet.getUser().getListedCount());
				
		}
		
		return features;
	}

}
