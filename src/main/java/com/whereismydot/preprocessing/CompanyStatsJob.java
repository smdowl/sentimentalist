package com.whereismydot.preprocessing;

import com.whereismydot.dataobjects.AugStatus;
import com.whereismydot.utils.CompanyClassifier;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import twitter4j.TwitterException;

import java.io.IOException;
import java.util.Iterator;

public class CompanyStatsJob extends MapReduceBase implements
        Reducer<Text, Text, Text, Text>,
        Mapper<LongWritable, Text, Text, Text> {

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

        Integer count = 0;
        while (values.hasNext()) {

            Text value = values.next();
            count++;

//            AugStatus status;
//
//            try {
//                status = new AugStatus(value.toString());
//            } catch (TwitterException e) {
//                e.printStackTrace();
//                continue;
//            }
        }

        out.collect(key, new Text(count.toString()));
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
