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

    private static int INDIFFERENT_SENTIMENT = 2;

    public static int getSentiment(String text) {
        Properties properties = new Properties();
        properties.setProperty("annotators", "tokenize, ssplit, parse, sentiment");

        // Hacky way of silencing all the logs.
        PrintStream err = System.err;
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {}
        }));

        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);

        int mainSentiment = 0;

        if (text != null && text.length() > 0) {
            int longest = 0;
            Annotation annotation = pipeline.process(text);
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

        System.setErr(err);

        // OK seems to return 2 so reset this to 0
        mainSentiment -= INDIFFERENT_SENTIMENT;

        return mainSentiment;
    }

    public static void main(String[] args) {

        String[] sentences = {
                "today is absolutely wonderfully beautiful",
                "today is very nice",
                "today is ok",
                "today is a horrible day"
        };

        for (String s : sentences)
            System.out.println(s + ": " + getSentiment(s));
    }
}
