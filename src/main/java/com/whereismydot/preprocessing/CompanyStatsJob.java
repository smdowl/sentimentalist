package com.whereismydot.preprocessing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.whereismydot.dataobjects.AugStatus;
import com.whereismydot.utils.CompanyClassifier;
import com.whereismydot.utils.Counter;
import com.whereismydot.utils.SentimentAnalyser;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import twitter4j.HashtagEntity;
import twitter4j.TwitterException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public class CompanyStatsJob extends MapReduceBase implements
        Reducer<Text, Text, Text, Text>,
        Mapper<LongWritable, Text, Text, Text> {

    private static int MIN_WORDCOUNT = 2;

    private static CompanyClassifier classifier = new CompanyClassifier();

    @Override
    public void map(LongWritable key, Text value, OutputCollector<Text, Text> out, Reporter reporter)
            throws IOException {

        AugStatus status = AugStatus.parseOrNull(value.toString());

        String companyMention = classifier.getCompanyMentioned(status.tweet);

        if (companyMention != null)
            out.collect(new Text(companyMention), value);
    }

    @Override
    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text,
            Text> out, Reporter reporter) throws IOException {

        double count = 0.0;

        Counter<String> hashtags = new Counter<String>();
        Counter<String> wordCounts = new Counter<String>();
        int sentiment = 0;

        while (values.hasNext()) {

            AugStatus status = AugStatus.parseOrNull(values.next().toString());

            for (HashtagEntity hashtag : status.tweet.getHashtagEntities()) {
                String tag = hashtag.getText();
                hashtags.increment(tag);
            }

            StringTokenizer tokenizer = new StringTokenizer(status.tweet.getText());

            while (tokenizer.hasMoreTokens())
                wordCounts.increment(tokenizer.nextToken());

            sentiment += SentimentAnalyser.getSentiment(status.tweet.getText());

            count++;

        }

        if (count == 0)
            return;

        wordCounts.filterCounts(MIN_WORDCOUNT);

        Gson gson = new Gson();

        JsonObject output = new JsonObject();
        output.add("count", new JsonPrimitive(count));
        output.add("hashtags", gson.toJsonTree(hashtags.counts));
        output.add("ave_sentiment", new JsonPrimitive(sentiment / count));
//        output.add("word_counts", gson.toJsonTree(wordCounts.counts));

        out.collect(key, new Text(output.toString()));
    }

    public static void main(String[] args) throws IOException {

        JobConf job = new JobConf(CompanyStatsJob.class);
        job.setMapperClass(CompanyStatsJob.class);
        job.setReducerClass(CompanyStatsJob.class);

        job.setMapOutputKeyClass(Text.class);
        job.setInputFormat(TextInputFormat.class);
        job.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(job, new Path("/Users/shaundowling/Google Drive/UCL/IRDM/groupcw/example/slice.json"));
        FileOutputFormat.setOutputPath(job, new Path("/Users/shaundowling/Google Drive/UCL/IRDM/groupcw/example/company"));

        JobClient.runJob(job);
    }
}
