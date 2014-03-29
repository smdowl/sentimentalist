package com.whereismydot.extractors;

import twitter4j.Status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * Class for generating features relating the tokens contained within the Status.
 */
public class TokenFeatures implements FeatureExtractor {

    @Override
    public Map<String, Double> extract(List<Status> tweets) {
        Map<String, Double> features = new HashMap<String, Double>();

        for (Status tweet : tweets) {
            addTokenCounts(features, tweet.toString());
        }

        return features;
    }

    private void addTokenCounts(Map<String, Double> counts, String tweet) {
        StringTokenizer st = new StringTokenizer(tweet);

        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            if (!counts.containsKey(token))
                counts.put(token, 0.0);

            counts.put(token, counts.get(token) + 1);
        }
    }
}
