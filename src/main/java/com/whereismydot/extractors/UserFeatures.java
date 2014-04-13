package com.whereismydot.extractors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import twitter4j.Status;

/**
 * Class for marking the users that have posted in the given tweets collection.
 */
public class UserFeatures implements FeatureExtractor {

	@Override
	public Map<String, Double> extract(List<Status> tweets) {
		Map <String,Double> features = new HashMap<String, Double>();
        
		for (Status tweet : tweets){
			features.put("user:" + tweet.getUser().getId(), 1.0);
		}
		
		return features;
	}

}
