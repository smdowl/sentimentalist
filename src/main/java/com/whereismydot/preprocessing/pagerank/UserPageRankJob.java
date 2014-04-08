package com.whereismydot.preprocessing.pagerank;

import com.google.gson.Gson;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class UserPageRankJob extends MapReduceBase implements
        Mapper<LongWritable, Text, LongWritable, Text>,
        Reducer<LongWritable, Text, LongWritable, Text> {

    private Gson gson = new Gson();

    @Override
    public void map(LongWritable key, Text value, OutputCollector<LongWritable, Text> out, Reporter reporter)
            throws IOException {

        String[] comps = value.toString().split("\t");
        Long userId = Long.parseLong(comps[0]);
        String json = comps[1];

        // For whatever reason this parses the input as doubles
        List<Double> doubleList = new LinkedList<>();
        doubleList = (List<Double>) gson.fromJson(json, doubleList.getClass());

        List<Long> adjacenyList = new LinkedList<>();
        for (Double d : doubleList)
            adjacenyList.add(d.longValue());

        Double p = 1.0 / adjacenyList.size();

        out.collect(new LongWritable(userId), new Text(json));

        for (Long otherId : adjacenyList) {
            out.collect(new LongWritable(otherId), new Text(p.toString()));
        }
    }

    @Override
    public void reduce(LongWritable key, Iterator<Text> values, OutputCollector<LongWritable, Text> out,
                       Reporter reporter) throws IOException {
        while (values.hasNext())
            out.collect(key, values.next());
    }

    public static void main(String[] args) throws IOException {

        JobConf job = new JobConf(UserPageRankJob.class);
        job.setMapperClass(UserPageRankJob.class);
        job.setReducerClass(UserPageRankJob.class);

        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);

        job.setInputFormat(TextInputFormat.class);
        job.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        JobClient.runJob(job);
    }
}
