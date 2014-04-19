package com.whereismydot.preprocessing.pagerank;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

class InDegreeMapper extends Mapper<Text, Text, Text, LongWritable> {
    @Override
    public void map(Text userId, Text value, Context context)
            throws IOException, InterruptedException {

        Map<String, Object> node = Utils.parseNode(value.toString());

        List<String> adjacencyList = (List<String>) node.get("adjacency");

        LongWritable count = new LongWritable(1);

        for (String otherUserId : adjacencyList)
            context.write(new Text(otherUserId), count);
    }
}

class InDegreeReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
    @Override
    public void reduce(Text key, Iterable<LongWritable> values, Context context)
            throws IOException, InterruptedException {
        long count = 0;

        for (LongWritable value : values)
            count += value.get();

        context.write(key, new LongWritable(count));
    }
}

public class InDegreeJob {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        Job job = Job.getInstance(new Configuration());
        job.setMapperClass(InDegreeMapper.class);
        job.setReducerClass(InDegreeReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);

        job.setInputFormatClass(KeyValueTextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(false);
    }
}
