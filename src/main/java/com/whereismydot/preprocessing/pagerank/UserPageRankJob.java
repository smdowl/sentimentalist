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
        Mapper<Text, Text, Text, Text>,
        Reducer<Text, Text, Text, Text> {

    private Gson gson = new Gson();

    @Override
    public void map(Text userId, Text value, OutputCollector<Text, Text> out, Reporter reporter)
            throws IOException {

        List<String> adjacencyList = new LinkedList<>();
        adjacencyList = (List<String>) gson.fromJson(value.toString(), adjacencyList.getClass());

        Double p = 1.0 / adjacencyList.size();

        out.collect(userId, value);

        for (String otherId : adjacencyList) {
            out.collect(new Text(otherId), new Text(p.toString()));
        }
    }

    @Override
    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> out,
                       Reporter reporter) throws IOException {

        while (values.hasNext())
            out.collect(key, values.next());

    }

    public static void main(String[] args) throws IOException {

        JobConf job = new JobConf(UserPageRankJob.class);
        job.setMapperClass(UserPageRankJob.class);
        job.setReducerClass(UserPageRankJob.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setInputFormat(KeyValueTextInputFormat.class);
        job.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        JobClient.runJob(job);
    }
}
