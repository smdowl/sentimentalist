package com.whereismydot.extractors;

import twitter4j.Status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * Class for generating features relating the word-tokens contained within the Status (text), and generally
 * features concerning single tweet-tokens in a specific time-bin (a token in itself).
 */
public class TokenFeatures implements FeatureExtractor {

    @Override
    public Map<String, Double> extract(List<Status> tweets) {
        Map<String, Double> features = new HashMap<String, Double>();

        double favouriteCount = 0.0;
        double retweetCount = 0.0;
        double isFavourited = 0.0;
        double isRetweeted = 0.0;
        double isRetweet = 0.0;
        String texts = "";
        
        for (Status tweet : tweets) {
            //addTokenCounts(features, tweet.toString());
        	favouriteCount += tweet.getFavoriteCount();
        	retweetCount += tweet.getRetweetCount();
        	isFavourited += tweet.isFavorited()? 1 : 0;
        	isRetweeted += tweet.isRetweeted()? 1 : 0;
        	isRetweet += tweet.isRetweet()? 1 : 0;
        	texts += tweet.getText() + " "; 
        	
        }
        
        // New Features:
    	features.put("Raw Times Favourited", favouriteCount);
    	features.put("Avg Times Favourited", favouriteCount/tweets.size());
    	features.put("Raw Retweet Count", retweetCount);
    	features.put("Avg Retweet Count", retweetCount/tweets.size());
    	features.put("Raw isFavourited Count", isFavourited);
    	features.put("Avg isFavourited Count", isFavourited/tweets.size());
    	features.put("Raw isRetweeted Count", isRetweeted);
    	features.put("Avg isRetweeted Count", isRetweeted/tweets.size());
    	features.put("Raw isRetweet Count", isRetweet);
    	features.put("Avg isRetweet Count", isRetweet/tweets.size());
    	
    	// adds to the features the word count over all words in all tweets in the given time-bin:
    	addTokenCounts(features, texts, tweets.size());
    	
        return features;
    }

    private void addTokenCounts(Map<String, Double> counts, String tweet, int binSize) {
        StringTokenizer st = new StringTokenizer(tweet);

        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            if (!counts.containsKey(token))
                counts.put(token, 0.0);

            //counts.put(token, counts.get(token) + 1); 
            // I suggest using normalised counts to control for varying time-bin size:
            counts.put(token, counts.get(token) + 1.0/binSize);
        }
    }
}
