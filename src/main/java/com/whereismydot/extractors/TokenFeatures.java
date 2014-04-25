package com.whereismydot.extractors;

import com.whereismydot.utils.SentimentAnalyser;
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

	private final SentimentAnalyser analyser = new SentimentAnalyser();
	
    @Override
    public Map<String, Double> extract(List<Status> tweets) {
        Map<String, Double> features = new HashMap<String, Double>();

        double favouriteCount = 0.0;
        double retweetCount = 0.0;
        double isFavourited = 0.0;
        double isRetweeted = 0.0;
        double isRetweet = 0.0;
        double sentiment = 0.0;
        String texts = "";
        Map<Status, Integer> tweetSentiment = analyser.getTweetSentiments(tweets);
        
        for (Status tweet : tweets) {
            //addTokenCounts(features, tweet.toString());
        	favouriteCount += tweet.getFavoriteCount();
        	retweetCount += tweet.getRetweetCount();
        	isFavourited += tweet.isFavorited()? 1 : 0;
        	isRetweeted += tweet.isRetweeted()? 1 : 0;
        	isRetweet += tweet.isRetweet()? 1 : 0;
        	texts += tweet.getText() + " ";
            sentiment += tweetSentiment.get(tweet);
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
        features.put("ave-sentiment", sentiment/tweets.size());

    	// adds to the features the word count over all words in all tweets in the given time-bin:
        addTokenCounts(features, texts, tweets.size());

        return features;
    }

    private void addTokenCounts(Map<String, Double> counts, String tweet, int binSize) {
        StringTokenizer st = new StringTokenizer(tweet);

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            String key = "token-count:" + token;

            if (!counts.containsKey(key))
                counts.put(key, 0.0);

            //counts.put(token, counts.get(token) + 1);
            // I suggest using normalised counts to control for varying time-bin size:
            counts.put(key, counts.get(key) + 1.0/binSize);
        }
    }
}
