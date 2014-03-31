package com.whereismydot.preprocessing;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.whereismydot.dataobjects.AugStatus;
import com.whereismydot.utils.SentimentAnalyser;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import twitter4j.HashtagEntity;

import java.io.IOException;
import java.util.Iterator;

public class HashtagStatsJob extends MapReduceBase implements
        Reducer<Text, Text, Text, Text>,
        Mapper<LongWritable, Text, Text, Text> {

    @Override
    public void map(LongWritable key, Text value, OutputCollector<Text, Text> out, Reporter reporter)
            throws IOException {

        AugStatus status = AugStatus.parseOrNull(value.toString());

        for (HashtagEntity hashtag : status.tweet.getHashtagEntities()) {
            String tag = hashtag.getText();
            out.collect(new Text(tag), value);
        }
    }

    @Override
    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text,
            Text> out, Reporter reporter) throws IOException {

        double count = 0.0;
        int sentiment = 0;

        while (values.hasNext()) {

            AugStatus status = AugStatus.parseOrNull(values.next().toString());

            sentiment += SentimentAnalyser.getSentiment(status.tweet.getText());

            count++;

        }

        if (count == 0)
            return;

        JsonObject output = new JsonObject();
        output.add("count", new JsonPrimitive(count));
        output.add("ave_sentiment", new JsonPrimitive(sentiment / count));

        out.collect(key, new Text(output.toString()));
    }

    public static void main(String[] args) throws IOException {

        JobConf job = new JobConf(HashtagStatsJob.class);
        job.setMapperClass(HashtagStatsJob.class);
        job.setReducerClass(HashtagStatsJob.class);

        job.setMapOutputKeyClass(Text.class);
        job.setInputFormat(TextInputFormat.class);
        job.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(job, new Path("/Users/shaundowling/Google Drive/UCL/IRDM/groupcw/example/slice.json"));
        FileOutputFormat.setOutputPath(job, new Path("/Users/shaundowling/Google Drive/UCL/IRDM/groupcw/example/hashtags"));

        JobClient.runJob(job);
    }
}
