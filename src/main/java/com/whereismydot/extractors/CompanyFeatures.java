package com.whereismydot.extractors;

import com.whereismydot.utils.CompanyClassifier;
import twitter4j.Status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanyFeatures implements FeatureExtractor {

    private CompanyClassifier classifier = new CompanyClassifier();

    @Override
    public Map<String, Double> extract(List<Status> tweets) {

        Map<String, Double> output = new HashMap<>();

        for (Status tweet : tweets) {
            String company = classifier.getCompanyMentioned(tweet);
            output.put("company:" + company, 1.0);
        }

        return output;
    }
}
