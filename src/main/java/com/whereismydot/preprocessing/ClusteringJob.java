package com.whereismydot.preprocessing;

import com.whereismydot.dataobjects.AugStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.json.DataObjectFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

public class ClusteringJob extends MapReduceBase implements
        Reducer<LongWritable, Text, LongWritable, Text>,
        Mapper<LongWritable, Text, LongWritable, Text> {

    @Override
    public void map(LongWritable key, Text value, OutputCollector<LongWritable, Text> out, Reporter reporter)
            throws IOException {

        out.collect(key, value);
    }

    @Override
    public void reduce(LongWritable key, Iterator<Text> values, OutputCollector<LongWritable,
            Text> out, Reporter reporter) throws IOException {

        while (values.hasNext()) {

            Text value = values.next();

            AugStatus status;

            try {
                status = new AugStatus(new StringReader(value.toString()));
            } catch (TwitterException e) {
                e.printStackTrace();
                continue;
            }

            out.collect(key, new Text(status.toString()));
        }
    }

    public static void main(String[] args) throws IOException {

        JobConf job = new JobConf(ClusteringJob.class);
        job.setMapperClass(ClusteringJob.class);
        job.setReducerClass(ClusteringJob.class);

        job.setInputFormat(TextInputFormat.class);
        job.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(job, new Path("/Users/shaundowling/Google Drive/UCL/IRDM/groupcw/example/example_tweet.json"));
        FileOutputFormat.setOutputPath(job, new Path("/Users/shaundowling/Google Drive/UCL/IRDM/groupcw/example/clustering"));

        JobClient.runJob(job);
    }
}
