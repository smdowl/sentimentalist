package com.whereismydot.preprocessing;

import com.whereismydot.dataobjects.AugStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import twitter4j.TwitterException;
import twitter4j.User;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

public class UsersJob extends MapReduceBase implements
        Reducer<LongWritable, Text, LongWritable, Text>,
        Mapper<LongWritable, Text, LongWritable, Text> {

    @Override
    public void map(LongWritable key, Text value, OutputCollector<LongWritable, Text> out, Reporter reporter)
            throws IOException {

        AugStatus status = getStatus(value);

        if (status == null)
            return;

        User user = status.tweet.getUser();
        LongWritable userId = new LongWritable(user.getId());

        out.collect(userId, value);
    }

    @Override
    public void reduce(LongWritable key, Iterator<Text> values, OutputCollector<LongWritable,
            Text> out, Reporter reporter) throws IOException {

        while (values.hasNext()) {
            AugStatus status = getStatus(values.next());

            if (status == null)
                continue;

            out.collect(key, new Text(status.toString()));
        }
    }

    private AugStatus getStatus(Text value) {
        AugStatus status = null;

        try {
            status = new AugStatus(new StringReader(value.toString()));
        } catch (TwitterException e) {
            e.printStackTrace();
        } finally {
            return status;
        }
    }

    public static void main(String[] args) throws IOException {

        JobConf job = new JobConf(UsersJob.class);
        job.setMapperClass(UsersJob.class);
        job.setReducerClass(UsersJob.class);

        job.setInputFormat(TextInputFormat.class);
        job.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(job, new Path("/Users/shaundowling/Google Drive/UCL/IRDM/groupcw/example/example_tweet.json"));
        FileOutputFormat.setOutputPath(job, new Path("/Users/shaundowling/Google Drive/UCL/IRDM/groupcw/example/users"));

        JobClient.runJob(job);
    }
}
