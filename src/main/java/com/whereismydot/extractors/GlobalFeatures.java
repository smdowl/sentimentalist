package com.whereismydot.extractors;

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

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import twitter4j.Logger;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

/**
 * This class extracts "global" features about all the users in the dataset.
 * Features implemented:
 * Average number of retweets on one's own posts
 * Proportion of retweets of others' posts and original posts.
 * 
 * @author Ale
 */

public class GlobalFeatures extends MapReduceBase implements
		Mapper<LongWritable, Text, Text, DoubleWritable>,
		Reducer<Text, DoubleWritable, Text, DoubleWritable> {
	
	private final TweetFilter filters[];
	
	public GlobalFeatures() {
	
		// Filter config
		this.filters    = new TweetFilter[0];
		
	}
		
	@Override
	public void map(LongWritable key, Text value, OutputCollector<Text, DoubleWritable> out, Reporter reporter)
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
        
        double isARetweet = tweet.isRetweet()? 1.0: 0.0;

        out.collect(new Text(tweet.getUser().getId() + " isRetweet"), new DoubleWritable(isARetweet));
        
        out.collect(new Text(tweet.getUser().getId() + " retweets"), new DoubleWritable(tweet.getRetweetCount()));
        
	}

    private boolean isRelevant(Status tweet) {
        for (TweetFilter filter : filters) {
            if (!filter.isRelevant(tweet))
                return false;
        }

        return true;
    }


	@Override
	public void reduce(Text userFeat, Iterator<DoubleWritable> counts,
			OutputCollector<Text, DoubleWritable> out, Reporter reporter)
			throws IOException {
		
		// Add the counts up:
		double result = 0.0;
		double count = 0.0;
		while (counts.hasNext()) {
			result += counts.next().get();
			count++;
		}
		
		out.collect(userFeat, new DoubleWritable(result/count));
	}

	private void error(String msg, Exception e){
		Logger.getLogger(getClass()).error(msg, e);
	}
	
	public static void main( String[] args ) throws IOException{
	    
        JobConf job = new JobConf(GlobalFeatures.class);
        job.setMapperClass(GlobalFeatures.class);
        job.setReducerClass(GlobalFeatures.class);
        
        job.setInputFormat(TextInputFormat.class);
        job.setOutputFormat(TextOutputFormat.class);
        
        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        
        JobClient.runJob(job);
    }
}
