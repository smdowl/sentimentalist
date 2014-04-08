package com.whereismydot.preprocessing.pagerank;

import com.google.gson.Gson;
import com.whereismydot.utils.TwitterParser;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import twitter4j.Status;
import twitter4j.User;

import java.io.IOException;
import java.util.*;


/**
 * Job that takes as input all the tweets and creates a weighted directed graph based on which users follow who.
 * i.e. user A follows user B and C => A: [B, C]
 */
public class UserGraphJob extends MapReduceBase implements
        Mapper<LongWritable, Text, LongWritable, LongWritable>,
        Reducer<LongWritable, LongWritable, LongWritable, Text> {

    @Override
    public void map(LongWritable key, Text value, OutputCollector<LongWritable, LongWritable> out, Reporter reporter)
            throws IOException {
        Status status = TwitterParser.parseOrNull(value.toString());

        Status retweetFrom = status.getRetweetedStatus();

        if (retweetFrom == null)
            return;

        User user = status.getUser();
        LongWritable userId = new LongWritable(user.getId());

        out.collect(userId, new LongWritable(retweetFrom.getUser().getId()));
    }

    /**
     * Take all followed users and output a map of transition (or retweet) probabilities for this user
     */
    @Override
    public void reduce(LongWritable userId, Iterator<LongWritable> followIds, OutputCollector<LongWritable, Text> out,
                       Reporter reporter) throws IOException {

        Set<Long> adjacencyList = new HashSet<>();

        while (followIds.hasNext()) {
            LongWritable followId = followIds.next();
            adjacencyList.add(followId.get());
        }

        Map<Long, Double> transitions = new HashMap<>();
        for (Long id : adjacencyList) {
            double transitionProb = 1.0/adjacencyList.size();
            transitions.put(id, transitionProb);
        }

        Gson gson = new Gson();
        String json = gson.toJson(transitions);

        out.collect(userId, new Text(json));
    }

    public static void main(String[] args) throws IOException {

        JobConf job = new JobConf(UserGraphJob.class);
        job.setMapperClass(UserGraphJob.class);
        job.setReducerClass(UserGraphJob.class);

        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setInputFormat(TextInputFormat.class);
        job.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        JobClient.runJob(job);
    }
}
