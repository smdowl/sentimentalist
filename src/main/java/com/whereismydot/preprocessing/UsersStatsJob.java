package com.whereismydot.preprocessing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import com.whereismydot.utils.Counter;

import com.whereismydot.utils.TwitterParser;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import twitter4j.*;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

public class UsersStatsJob extends MapReduceBase implements
        Reducer<LongWritable, Text, LongWritable, Text>,
        Mapper<LongWritable, Text, LongWritable, Text> {

    @Override
    public void map(LongWritable key, Text value, OutputCollector<LongWritable, Text> out, Reporter reporter)
            throws IOException {

        Status status = TwitterParser.parseOrNull(value.toString());

        User user = status.getUser();
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

        Counter<String> hashtags = new Counter<String>();
        Counter<Long> userMentions = new Counter<Long>();
        Counter<Long> repliedTo = new Counter<Long>();
        Counter<String> urls = new Counter<String>();

        while (values.hasNext()) {

            Status status = TwitterParser.parseOrNull(values.next().toString());

            count++;

            aveTweetLength += status.getText().length();
            retweetCount += status.getRetweetCount();
            favouritesCount += status.getFavoriteCount();

            for (HashtagEntity hashtag : status.getHashtagEntities()) {
                String tag = hashtag.getText();
                hashtags.increment(tag);
            }

            for (UserMentionEntity mention : status.getUserMentionEntities()) {
                Long userId = mention.getId();
                userMentions.increment(userId);
            }

            for (URLEntity urlEntity : status.getURLEntities()) {
                URL url = new URL(urlEntity.getExpandedURL());
                urls.increment(url.getHost());
            }

            Long repliedId = status.getInReplyToUserId();
            if (repliedId > 0)
                repliedTo.increment(repliedId);
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
        output.add("hashtags", gson.toJsonTree(hashtags.counts));
        output.add("user_mentions", gson.toJsonTree(userMentions.counts));
        output.add("urls_mentions", gson.toJsonTree(urls.counts));
        output.add("replied_to", gson.toJsonTree(repliedTo.counts));

        out.collect(key, new Text(output.toString()));
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
