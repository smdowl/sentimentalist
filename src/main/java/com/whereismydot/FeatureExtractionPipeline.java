package com.whereismydot;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.whereismydot.extractors.FeatureExtractor;
import com.whereismydot.extractors.TokenFeatures;
import com.whereismydot.extractors.UserFeatures;
import com.whereismydot.filters.TweetFilter;
import com.whereismydot.utils.TimeBinner;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import twitter4j.Logger;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

public class FeatureExtractionPipeline extends MapReduceBase implements
		Reducer<LongWritable, Text, LongWritable, String>, 
		Mapper<LongWritable, Text, LongWritable, Text> {

	private final TweetFilter filters[];
	private final FeatureExtractor extractors[];
	private final TimeBinner timeBinner;
	
	/**
	 * This is the constructor that's called by the Hadoop framework so configure 
	 * whatever extractors or filters you plan on using in here.
	 */
	public FeatureExtractionPipeline(){
	
		// Filter config
		this.filters    = new TweetFilter[0];
		
		// Extractor config 
		this.extractors = new FeatureExtractor[2];
		this.extractors[0] = new TokenFeatures();
		this.extractors[1] = new UserFeatures();
		
		// Time discretizer config
		this.timeBinner = new TimeBinner();
	}
		
	@Override
	public void map(LongWritable key, Text value, OutputCollector<LongWritable, Text> out, Reporter reporter)
			throws IOException {
		

        String json = value.toString();

        Status tweet;
        try {
            tweet = TwitterObjectFactory.createStatus(json);
        } catch (TwitterException e) {
            error("Failed to parse tweet in mapper.", e);
            return;
        }

        // Filter out irrelevant tweets
        if (!isRelevant(tweet))
            return;

        long timeBin   = timeBinner.timeBin(tweet.getCreatedAt());
        LongWritable t = new LongWritable(timeBin);
        out.collect(t, value);
	}

    private boolean isRelevant(Status tweet) {
        for (TweetFilter filter : filters) {
            if (!filter.isRelevant(tweet))
                return false;
        }

        return true;
    }

    /**
     * Load all tweets into memory, assuming that the number of tweets for a given time slice will
     * not exceed memory limits. Then, create a full feature vector from all tweets and output the
     * result/
     */
	@Override
	public void reduce(LongWritable time, Iterator<Text> tweetJsonIter,
			OutputCollector<LongWritable, String> out, Reporter reporter)
			throws IOException {
		
		List<Status> tweets = parseTweets(tweetJsonIter);
		
		// Now get features out of them
		Map<String, Double> result = new HashMap<String, Double>();
		for (FeatureExtractor extractor : extractors){
			result.putAll(extractor.extract(tweets));
		}
		
		out.collect(time, new Gson().toJson(result));
	}

    private List<Status> parseTweets(Iterator<Text> tweetJsonIter) {
        List<Status> tweets = new ArrayList<Status>();

        // First parse all the tweets
        while (tweetJsonIter.hasNext()) {
            try {
                tweets.add(TwitterObjectFactory.createStatus(tweetJsonIter.next().toString()));
            } catch (TwitterException e) {
                error("Failed to parse tweet in the reducer.", e);
            }
        }

        return tweets;
    }

	private void error(String msg, Exception e){
		Logger.getLogger(getClass()).error(msg, e);
	}
}
