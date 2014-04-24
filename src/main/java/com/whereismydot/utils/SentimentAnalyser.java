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
import java.util.Properties;

public class SentimentAnalyser {

    private static SentimentAnalyser analyser = new SentimentAnalyser();

    // OK seems to return 2 but then from experimentation the most common is 1
    private static int INDIFFERENT_SENTIMENT = 1;

    private StanfordCoreNLP pipeline;

    private SentimentAnalyser() {
        Properties properties = new Properties();
        properties.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        pipeline = new StanfordCoreNLP(properties);
    }

    public static int getSentiment(String text) {
        int mainSentiment = 0;

        if (text != null && text.length() > 0) {
            int longest = 0;
            Annotation annotation = analyser.pipeline.process(text);
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
