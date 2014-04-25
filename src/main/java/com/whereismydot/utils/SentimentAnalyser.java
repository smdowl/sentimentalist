package com.whereismydot.utils;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import twitter4j.Status;

public class SentimentAnalyser {

    // OK seems to return 2 but then from experimentation the most common is 1
    private static int INDIFFERENT_SENTIMENT = 1;

    // Store a per thread version of the pipeline
    private static final ThreadLocal<StanfordCoreNLP> pipelineRef = new ThreadLocal<StanfordCoreNLP>();    

    /** 
     * Get the sentiment of a single piece of text. You should use the batch 
     * methods below for better performance.
     * @param text
     * @return
     */
    public int getSentiment(String text) {
        int mainSentiment = 0;

        if (text != null && text.length() > 0) {
            int longest = 0;
            Annotation annotation = getThreadPipeline().process(text);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);

                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentence.toString();
                if (partText.length() > longest) {
                    mainSentiment = sentiment;
                    longest = partText.length();
                }

            }
        }

        mainSentiment -= INDIFFERENT_SENTIMENT;

        return mainSentiment;
    }

    /**
     * Extract sentiment using multiple threads.
     * @param tweets
     * @return
     */
    public Map<Status, Integer> getTweetSentiments(List<Status> tweets){
    	final Map<Status, Integer> result = new ConcurrentHashMap<Status, Integer>();
    	
    	// Change number of threads here.
    	ExecutorService executor = Executors.newFixedThreadPool(8);

    	for(final Status tweet : tweets){
    		executor.execute(new Runnable() {
				
				@Override
				public void run() {
					result.put(tweet, getSentiment(tweet.getText()));					
				}
			});
    	}
    	
    	try {
    		executor.shutdown();
			executor.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException("Unexpected interruption");
		}
    	
    	return result;
    }
    
    public Map<String, Integer> getSentiments(List<String> texts){
    	final Map<String, Integer> result = new ConcurrentHashMap<String, Integer>();
    	
    	// Change number of threads here.
    	ExecutorService executor = Executors.newFixedThreadPool(8);

    	for(final String text : texts){
    		executor.execute(new Runnable() {
				
				@Override
				public void run() {
					result.put(text, getSentiment(text));					
				}
			});
    	}
    	
    	try {
    		executor.shutdown();
			executor.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException("Unexpected interruption");
		}
    	
    	return result;
    }
    
    /**
     * 
     * @return Returns the pipeline assigned to the current thread.
     */
    private StanfordCoreNLP getThreadPipeline(){
    	StanfordCoreNLP pipeline = pipelineRef.get();
    	if(pipeline == null){
    		Properties properties = new Properties();
    	    properties.setProperty("annotators", "tokenize,ssplit,pos,parse,sentiment");
    	    pipeline = new StanfordCoreNLP(properties);
    	    pipelineRef.set(pipeline);     
    	}
    	
    	return pipeline;
    }
    
    public static void main(String[] args) {

        String[] sentences = {
                "absolutely wonderfully fantastic",
                "very nice",
                "ok",
                "pretty bad",
                "disgusting, terrible and awful"
        };

        SentimentAnalyser analyser = new SentimentAnalyser();

        for (String s : sentences)
            System.out.println(s + ": " + analyser.getSentiment(s));
    }
}
