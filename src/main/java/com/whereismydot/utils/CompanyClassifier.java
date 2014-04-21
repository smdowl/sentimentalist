package com.whereismydot.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import experimentation.TwitterTest;
import twitter4j.Status;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CompanyClassifier {

    private Map<String, Set<String>> companyKeywords;
    private Map<String, Long> companyKeys;

    public CompanyClassifier() {
        loadCompanyMap();
    }

    private void loadCompanyMap() {
        companyKeywords = new HashMap<>();
        companyKeys = new HashMap<>();

        // Load the map defined in resources
        InputStream input = this.getClass().getResourceAsStream("/company_map.json");
        JsonReader reader = new JsonReader(new InputStreamReader(input));

        // The object is just a dict so load it into an Object
        JsonParser parser = new JsonParser();
        JsonObject elem =  parser.parse(reader).getAsJsonObject();

        long key = 1;

        for (Map.Entry<String, JsonElement> entry : elem.entrySet()) {

            String companyName = entry.getKey();
            companyKeys.put(companyName, key++);
            companyKeywords.put(companyName, new HashSet<String>());

            // We are storing the lower case of every string match for consistency
            for (JsonElement comp : entry.getValue().getAsJsonArray())
                companyKeywords.get(companyName).add(comp.getAsString().toLowerCase());

        }
    }

    /**
     * Returns the first company that has a keyword is contained in the input tweets text.
     */
    public String getCompanyMentioned(Status tweet) {

        // We always match lower case strings
        String tweetText = tweet.toString().toLowerCase();

        for (String company : companyKeywords.keySet()) {
            for (String keyword : companyKeywords.get(company)) {

                if (tweetText.contains(keyword))
                    return company;

            }
        }

        return null;
    }

    public long getCompanyKey(Status tweet) {
        String company = getCompanyMentioned(tweet);

        if (company == null)
            return -1;

        return companyKeys.get(company);
    }

    public static void main(String[] args) {
        CompanyClassifier classifier =  new CompanyClassifier();
        Status testStatus = TwitterTest.getExampleTweet();

        // Should find company in tweet
        assert classifier.getCompanyMentioned(testStatus).equals("walt disney");
        System.out.println(classifier.getCompanyKey(testStatus));
        System.out.println("Success!");
    }

}
