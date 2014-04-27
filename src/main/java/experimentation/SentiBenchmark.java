package experimentation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import com.whereismydot.utils.SentimentAnalyser;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class SentiBenchmark {

    private StanfordCoreNLP pipeline;
    private static int INDIFFERENT_SENTIMENT = 1;

	public static void main(String[] args) throws IOException, TwitterException {
		if(args.length != 1){
			return;
		}
		List<Status> tweets = new ArrayList<Status>();
		for(String line : Files.readAllLines(Paths.get(args[0]), StandardCharsets.UTF_8)){
            tweets.add(TwitterObjectFactory.createStatus(line));

		}
		
		new SentiBenchmark().run(tweets);
	}
	
	public void run(List<Status> tweets){

		long start = System.nanoTime();
        Properties properties = new Properties();
        properties.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        pipeline = new StanfordCoreNLP(properties);
		long end = System.nanoTime();
		time("Init", start,end,1);
		
		SentimentAnalyser analyser = new SentimentAnalyser(); 
		start = System.nanoTime();
		analyser.getTweetSentiments(tweets);
			
		end = System.nanoTime();
		
		time("Per tweet", start, end, tweets.size());
	}

	private void time(String label, long start, long end, int iters){
		System.out.println(label + ":" + ((end - start) / (iters * 1000 * 1000)) + "ms");

	}
	

}
