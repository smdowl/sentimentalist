package com.whereismydot.utils;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

public class TwitterParser {

    public static Status parseOrNull(String tweetString) {
        Status status = null;

        try {
            status = TwitterObjectFactory.createStatus(tweetString);
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return status;
    }

}
