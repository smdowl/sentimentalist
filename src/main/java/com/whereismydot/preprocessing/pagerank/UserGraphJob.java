package com.whereismydot.preprocessing.pagerank;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
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

    @Override
    public void reduce(LongWritable userId, Iterator<LongWritable> followIds, OutputCollector<LongWritable, Text> out,
                       Reporter reporter) throws IOException {

        List<Long> adjacencyList = new LinkedList<Long>();

        while (followIds.hasNext()) {
            LongWritable followId = followIds.next();
            adjacencyList.add(followId.get());
        }

        Gson gson = new Gson();
        String json = gson.toJson(adjacencyList);

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

        FileInputFormat.setInputPaths(job, new Path("/Users/shaundowling/Google Drive/UCL/IRDM/groupcw/example/slice.json"));
        FileOutputFormat.setOutputPath(job, new Path("/Users/shaundowling/Google Drive/UCL/IRDM/groupcw/example/graph"));

        JobClient.runJob(job);
    }
}
