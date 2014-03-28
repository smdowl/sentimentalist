package com.whereismydot.dataobjects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class AugStatus {

    public Status tweet;
    public JsonObject obj;

    /**
     * Parse the tweet contained within the stream that the reader is reading.
     * In doing so we also tracks the underlying JSON object so that we can add fields as we see fit.
     * @param reader
     * @throws TwitterException
     */
    public AugStatus(Reader reader) throws TwitterException {

        // Instantiating these every time may take a while...
        JsonParser parser = new JsonParser();
        Gson gson = new Gson();

        JsonReader jsonReader = new JsonReader(reader);

        JsonElement elem =  parser.parse(jsonReader);

        // Store the underlying JSON for use later.
        // (I feel like storing this every time is a bit retarded but I think it's easiest)
        obj = elem.getAsJsonObject();
        String cleanJson = gson.toJson(elem);

        tweet = TwitterObjectFactory.createStatus(cleanJson);

    }

    public String toString() {
        return obj.toString();
    }

    public static void main(String[] args) throws TwitterException {
        InputStream in = AugStatus.class.getResourceAsStream("/example_tweet.json");
        AugStatus status = new AugStatus(new InputStreamReader(in));

        System.out.println(status);
    }
}
