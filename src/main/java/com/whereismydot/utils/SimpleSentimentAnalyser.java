package com.whereismydot.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.StringTokenizer;
import java.io.InputStream;
import java.io.InputStreamReader;

import twitter4j.Status;


public class SimpleSentimentAnalyser {

	private Map<String, Double> dictionary;

	public SimpleSentimentAnalyser()  {
		// This is our main dictionary representation
		dictionary = new HashMap<String, Double>();

		// From String to list of doubles.
		HashMap<String, HashMap<Integer, Double>> tempDictionary = new HashMap<String, HashMap<Integer, Double>>();

		BufferedReader csv = null;
		try {
			InputStream in = SimpleSentimentAnalyser.class.getResourceAsStream("/SWN.txt");
			csv = new BufferedReader(new InputStreamReader(in));
			
			int lineNumber = 0;

			String line;
			while ((line = csv.readLine()) != null) {
				lineNumber++;

				// If it's a comment, skip this line.
				if (!line.trim().startsWith("#")) {
					// We use tab separation
					String[] data = line.split("\t");
					String wordTypeMarker = data[0];

					// Example line:
					// POS ID PosS NegS SynsetTerm#sensenumber Desc
					// a 00009618 0.5 0.25 spartan#4 austere#3 ascetical#2
					// ascetic#2 practicing great self-denial;...etc

					// Is it a valid line? Otherwise, through exception.
					if (data.length != 6) {
						throw new IllegalArgumentException(
								"Incorrect tabulation format in file, line: "
										+ lineNumber);
					}

					// Calculate synset score as score = PosS - NegS
					Double synsetScore = Double.parseDouble(data[2])
							- Double.parseDouble(data[3]);

					// Get all Synset terms
					String[] synTermsSplit = data[4].split(" ");

					// Go through all terms of current synset.
					for (String synTermSplit : synTermsSplit) {
						// Get synterm and synterm rank
						String[] synTermAndRank = synTermSplit.split("#");
						String synTerm = synTermAndRank[0] + "#"
								+ wordTypeMarker;

						int synTermRank = Integer.parseInt(synTermAndRank[1]);
						// What we get here is a map of the type:
						// term -> {score of synset#1, score of synset#2...}

						// Add map to term if it doesn't have one
						if (!tempDictionary.containsKey(synTerm)) {
							tempDictionary.put(synTerm,
									new HashMap<Integer, Double>());
						}

						// Add synset link to synterm
						tempDictionary.get(synTerm).put(synTermRank,
								synsetScore);
					}
				}
			}

			// Go through all the terms.
			for (Map.Entry<String, HashMap<Integer, Double>> entry : tempDictionary
					.entrySet()) {
				String word = entry.getKey();
				Map<Integer, Double> synSetScoreMap = entry.getValue();

				// Calculate weighted average. Weigh the synsets according to
				// their rank.
				// Score= 1/2*first + 1/3*second + 1/4*third ..... etc.
				// Sum = 1/1 + 1/2 + 1/3 ...
				double score = 0.0;
				double sum = 0.0;
				for (Map.Entry<Integer, Double> setScore : synSetScoreMap
						.entrySet()) {
					score += setScore.getValue() / (double) setScore.getKey();
					sum += 1.0 / (double) setScore.getKey();
				}
				score /= sum;

				dictionary.put(word, score);
			}

			HashMap<String, HashMap<String, Double>> dictWithoutPos = new HashMap<String, HashMap<String, Double>>();
			
			for (String word : dictionary.keySet()) {
				String wordWithoutPos = word.substring(0, word.length() - 2);
				String pos = word.substring(word.length()-1,word.length());
				if(dictWithoutPos.containsKey(wordWithoutPos)){
					dictWithoutPos.get(wordWithoutPos).put(pos, dictionary.get(word));
				}
				else{
					HashMap<String,Double> posScorePair = new HashMap<String,Double>();
					posScorePair.put(pos, dictionary.get(word));
					dictWithoutPos.put(wordWithoutPos, posScorePair);
				}
			}
			for (String wordWithoutPos : dictWithoutPos.keySet()){
				double count = 0.0;
				double total = 0.0;
				for (Double score : dictWithoutPos.get(wordWithoutPos).values()){
					total+=score;
					count++;
				}
				dictionary.put(wordWithoutPos, total/count);
			}
				

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		finally {
			if (csv != null) {
				try {
					csv.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public double extractWithPos(String word, String pos) {
		return dictionary.get(word + "#" + pos);
	}

	public Map<Status, Double> getTweetSentiments(List<Status> tweets) {
		HashMap<Status, Double> sentimentMap = new HashMap<Status, Double>();
		for (Status tweet : tweets) {
			sentimentMap.put(tweet, getSentiment(tweet.getText()));
		}

		return sentimentMap;
	}

	public double getSentiment(String text) {
		double sentiment = 0.0;
		double count = 0.0;
		StringTokenizer tokenizer = new StringTokenizer(text);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();

			if (dictionary.containsKey(token)) {
				count++;
				sentiment += dictionary.get(token);
			}
		}
		double result = count==0.0? 0:sentiment / count;
		return result;
	}
	
	
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.err
					.println("Usage: java SentiWordNetDemoCode <pathToSentiWordNetFile>");
			return;
		}

		SimpleSentimentAnalyser sentiwordnet = new SimpleSentimentAnalyser();

		System.out
				.println("good#a " + sentiwordnet.extractWithPos("good", "a"));
		System.out.println("bad#a " + sentiwordnet.extractWithPos("bad", "a"));
		System.out
				.println("blue#a " + sentiwordnet.extractWithPos("blue", "a"));
		System.out
				.println("blue#n " + sentiwordnet.extractWithPos("blue", "n"));
		System.out.println("Test: "
				+ sentiwordnet.getSentiment("this company is not amazing"));
	}
	

}
