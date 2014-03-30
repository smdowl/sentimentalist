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

    public CompanyClassifier() {
        loadCompanyMap();
    }

    private void loadCompanyMap() {
        companyKeywords = new HashMap<String, Set<String>>();

        InputStream input = this.getClass().getResourceAsStream("/company_map.json");
        JsonReader reader = new JsonReader(new InputStreamReader(input));

        // Instantiating these every time may take a while...
        JsonParser parser = new JsonParser();

        JsonObject elem =  parser.parse(reader).getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : elem.entrySet()) {
            String companyName = entry.getKey();
            companyKeywords.put(companyName, new HashSet<String>());

            for (JsonElement comp : entry.getValue().getAsJsonArray()) {
                companyKeywords.get(companyName).add(comp.getAsString());
            }
        }
    }

    /**
     * Returns the first company that has a keyword is contained in the input tweets text.
     */
    public String getCompanyMentioned(Status tweet) {

        for (String company : companyKeywords.keySet()) {
            for (String keyword : companyKeywords.get(company)) {

                if (tweet.getText().contains(keyword))
                    return company;

            }
        }

        return null;
    }

    public static void main(String[] args) {
        CompanyClassifier classifier =  new CompanyClassifier();
        Status testStatus = TwitterTest.getExampleTweet();

        // Should find company in tweet
        assert classifier.getCompanyMentioned(testStatus).equals("walt disney");

        System.out.println("Success!");
    }

}
