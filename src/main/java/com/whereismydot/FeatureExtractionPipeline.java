package com.whereismydot;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
		Reducer<LongWritable, Text, LongWritable, Map<String, Double>>, 
		Mapper<LongWritable, Text, LongWritable, Text> {

	private final TweetFilter      filters[]; 
	private final FeatureExtractor extractors[];
	private final TimeBinner	   timeBinner;
	
	
	public FeatureExtractionPipeline(){
		this.filters    = new TweetFilter[0];
		this.extractors = new FeatureExtractor[0];
		this.timeBinner = new TimeBinner();
	}
		
	@Override
	public void map(LongWritable key, Text value, OutputCollector<LongWritable, Text> out, Reporter reporter)
			throws IOException {
		

		// Since the example file has the tweets as JSON obejects with no separators
		// between them I use this the GSON json parser to consume them one at a 
		// time and than re-serialize and pass them on to twitter4j.
		JsonReader reader = new JsonReader(new StringReader(value.toString()));
        reader.setLenient(true);

		JsonParser parser = new JsonParser();
		Gson gson = new Gson();
		long count = 0;
		
		try{
			while(reader.hasNext()){
				Status tweet;
				try {
					JsonElement elem =  parser.parse(reader);
		        
					String cleanJson = gson.toJson(elem);
					tweet = TwitterObjectFactory.createStatus(cleanJson);
				
					// Filter out irrelevant tweets
					for (TweetFilter filter : filters){
						if (!filter.isRelevant(tweet)){
							return;
						}
					}
					long timeBin   = timeBinner.timeBin(tweet.getCreatedAt());
					LongWritable t = new LongWritable(timeBin);
					Text         v = new Text(cleanJson);
					out.collect(t, v);
				
					reporter.progress();
					count++;
				
				} catch (TwitterException e) {
					error("Failed to parse tweet in mapper.", e);
				}
			}
		} catch(Exception e){
			if(reader.hasNext()){	
				String msg = "Failed to parse json -- assuming we have reached "
						   + "the end of valid input after " + count + " tweets.";
				
				error(msg, e);
			}
		}
			
		Logger.getLogger(getClass()).error("Done mapping");
	}

	@Override
	public void reduce(LongWritable time, Iterator<Text> tweetJsonIter,
			OutputCollector<LongWritable, Map<String, Double>> out, Reporter reporter)
			throws IOException {
		
		List<Status> tweets = new ArrayList<Status>();
		
		// First parse all the tweets
		while(tweetJsonIter.hasNext()){
			try {
				tweets.add(TwitterObjectFactory.createStatus(tweetJsonIter.next().toString()));
			} catch (TwitterException e) {
				error("Failed to parse tweet in the reducer.", e);
			}
		}
		
		// Now get features out of them
		Map<String, Double> result = new HashMap<String, Double>();
		for (FeatureExtractor extractor : extractors){
			result.putAll(extractor.extract(tweets));
		}
		
		out.collect(time, result);
	}

	private void error(String msg, Exception e){
		Logger.getLogger(getClass()).error(msg, e);
	}
}
