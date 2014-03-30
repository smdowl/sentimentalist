package com.whereismydot.preprocessing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.whereismydot.dataobjects.AugStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import twitter4j.HashtagEntity;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserMentionEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UsersStatsJob extends MapReduceBase implements
        Reducer<LongWritable, Text, LongWritable, Text>,
        Mapper<LongWritable, Text, LongWritable, Text> {

    @Override
    public void map(LongWritable key, Text value, OutputCollector<LongWritable, Text> out, Reporter reporter)
            throws IOException {

        AugStatus status;

        try {
            status = new AugStatus(value.toString());
        } catch (TwitterException e) {
            e.printStackTrace();
            return;
        }

        User user = status.tweet.getUser();
        LongWritable userId = new LongWritable(user.getId());

        out.collect(userId, value);
    }

    /**
     * Here was have all tweets from a single users. We want to accumulate statistics from these.
     */
    @Override
    public void reduce(LongWritable key, Iterator<Text> values, OutputCollector<LongWritable,
            Text> out, Reporter reporter) throws IOException {

        int count = 0;
        int retweetCount = 0;
        int favouritesCount = 0;
        double aveTweetLength = 0;

        Map<String, Integer> hashtags = new HashMap<String, Integer>();
        Map<Long, Integer> userMentions = new HashMap<Long, Integer>();
        Map<Long, Integer> replied = new HashMap<Long, Integer>();

        while (values.hasNext()) {
            AugStatus status;

            try {
                status = new AugStatus(values.next().toString());
            } catch (TwitterException e) {
                e.printStackTrace();
                continue;
            }

            count++;

            aveTweetLength += status.tweet.getText().length();

            retweetCount += status.tweet.getRetweetCount();

            favouritesCount += status.tweet.getFavoriteCount();

            for (HashtagEntity hashtag : status.tweet.getHashtagEntities()) {
                String tag = hashtag.getText();
                increment(hashtags, tag);
            }

            for (UserMentionEntity mention : status.tweet.getUserMentionEntities()) {
                Long userId = mention.getId();
                increment(userMentions, userId);
            }

            Long repliedId = status.tweet.getInReplyToUserId();
            increment(replied, repliedId);
        }

        // Make sure at least some json was well formed.
        if (count == 0)
            return;

        aveTweetLength /= count;

        Gson gson = new Gson();

        JsonObject output = new JsonObject();
        output.add("count", new JsonPrimitive(count));
        output.add("retweet_count", new JsonPrimitive(retweetCount));
        output.add("favourite_count", new JsonPrimitive(favouritesCount));
        output.add("ave_length", new JsonPrimitive(aveTweetLength));
        output.add("hashtags", gson.toJsonTree(hashtags));
        output.add("user_mentions", gson.toJsonTree(userMentions));

        out.collect(key, new Text(output.toString()));
    }

    private <K> void increment(Map<K, Integer> map, K key) {
        if (!map.containsKey(key))
            map.put(key, 0);

        map.put(key, map.get(key) + 1);
    }

    public static void main(String[] args) throws IOException {

        JobConf job = new JobConf(UsersStatsJob.class);
        job.setMapperClass(UsersStatsJob.class);
        job.setReducerClass(UsersStatsJob.class);

        job.setInputFormat(TextInputFormat.class);
        job.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(job, new Path("/Users/shaundowling/Google Drive/UCL/IRDM/groupcw/example/slice.json"));
        FileOutputFormat.setOutputPath(job, new Path("/Users/shaundowling/Google Drive/UCL/IRDM/groupcw/example/users"));

        JobClient.runJob(job);
    }
}
