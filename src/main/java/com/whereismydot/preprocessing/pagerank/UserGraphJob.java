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
        Mapper<LongWritable, Text, Text, Text>,
        Reducer<Text, Text, Text, Text> {

    @Override
    public void map(LongWritable key, Text value, OutputCollector<Text, Text> out, Reporter reporter)
            throws IOException {
        Status status = TwitterParser.parseOrNull(value.toString());

        Status retweetFrom = status.getRetweetedStatus();

        if (retweetFrom == null)
            return;

        User user = status.getUser();
        Text userId = new Text("" + user.getId());

        String retweetIdString = String.valueOf(retweetFrom.getUser().getId());
        out.collect(userId, new Text(retweetIdString));
    }

    /**
     * Take all followed users and output a map of transition (or retweet) probabilities for this user
     */
    @Override
    public void reduce(Text userId, Iterator<Text> retweetedIds, OutputCollector<Text, Text> out,
                       Reporter reporter) throws IOException {

        Set<String> adjacencyList = new HashSet<>();

        while (retweetedIds.hasNext()) {
            Text followId = retweetedIds.next();
            adjacencyList.add(followId.toString());
        }

        Map<String, Object> output = new HashMap<>();
        output.put("page_rank", 1.0);
        output.put("adjacency", adjacencyList);

        Gson gson = new Gson();
        String json = gson.toJson(output);

        out.collect(userId, new Text(json));
    }

    public static void main(String[] args) throws IOException {

        JobConf job = new JobConf(UserGraphJob.class);
        job.setMapperClass(UserGraphJob.class);
        job.setReducerClass(UserGraphJob.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setInputFormat(TextInputFormat.class);
        job.setOutputFormat(TextOutputFormat.class);

        String[] paths = args[0].split(",");
        Path[] pathsArr = new Path[paths.length];
        for (int i = 0; i < paths.length; i++)
            pathsArr[i] = new Path(paths[i]);

        FileInputFormat.setInputPaths(job, pathsArr);
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        JobClient.runJob(job);
    }
}
