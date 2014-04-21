package com.whereismydot;

import java.io.IOException;
import java.util.*;

import com.whereismydot.extractors.*;
import com.whereismydot.filters.TweetFilter;
import com.whereismydot.utils.CompanyClassifier;
import com.whereismydot.utils.CompanyTimeBinner;
import com.whereismydot.utils.TimeBinner;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import com.google.gson.Gson;

import twitter4j.Logger;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

public class FeatureExtractionPipeline extends MapReduceBase implements
		Reducer<LongWritable, Text, LongWritable, String>, 
		Mapper<LongWritable, Text, LongWritable, Text> {

	private final List<TweetFilter> filters = new LinkedList<>();
	private final List<FeatureExtractor> extractors = new LinkedList<>();

    private final CompanyTimeBinner binner = new CompanyTimeBinner();
    private final CompanyClassifier classifier = new CompanyClassifier();

    private enum Counters {
        NoCompanyTweet
    }

	/**
	 * This is the constructor that's called by the Hadoop framework so configure 
	 * whatever extractors or filters you plan on using in here.
	 */
	public FeatureExtractionPipeline(){

		// Extractor config 
		extractors.add(new TokenFeatures());
        extractors.add(new UserFeatures());
        extractors.add(new CompanyFeatures());
        extractors.add(new PageRankExtractor());
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

        // If, for some reason, we can't match this to a company then don't emit it.
        long companyKey = classifier.getCompanyKey(tweet);
        if (companyKey == -1) {
            reporter.getCounter(Counters.NoCompanyTweet).increment(1);
            return;
        }

        long timeBin   = binner.bin(tweet.getCreatedAt(), companyKey);
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
