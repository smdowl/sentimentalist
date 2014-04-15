package com.whereismydot.preprocessing.pagerank;

import com.google.gson.Gson;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.*;

enum Counter {
    USERS,
    LOST_MASS
}

class Utils {
    static private Gson gson = new Gson();
    static final double counterScale = 1e6;

    static Map<String, Object> parseNode(String json) {
        HashMap<String, Object> node = new HashMap<>();
        node = gson.fromJson(json, node.getClass());
        return node;
    }
}

class UserPageRankMapper extends Mapper<Text, Text, Text, Text> {

    @Override
    public void map(Text userId, Text value, Context context)
            throws IOException, InterruptedException {

        Map<String, Object> node = Utils.parseNode(value.toString());

        List<String> adjacencyList = (List<String>) node.get("adjacency");
        Double p = getPageRank(node) / adjacencyList.size();

        context.write(userId, value);

        for (String otherId : adjacencyList) {
            context.write(new Text(otherId), new Text(p.toString()));
        }

        if (adjacencyList.size() == 0) {
            long lostMass = (long) (getPageRank(node) * Utils.counterScale);
            context.getCounter(Counter.LOST_MASS).increment(lostMass);
        }
    }

    private double getPageRank(Map<String, Object> node) {
        return (double) node.get("page_rank");
    }
}

class UserPageRankReducer extends Reducer<Text, Text, Text, Text> {

    private Gson gson = new Gson();

    @Override
    public void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {

        Map<String, Object> node = null;
        Double sum = 0.0;

        for (Text value : values) {
            String next = value.toString();

            if (isNode(next))
                node = Utils.parseNode(next);
            else
                sum += Double.parseDouble(next);
        }

        if (node == null) {
            node = new HashMap<>();
            node.put("adjacency", new LinkedList<String>());
        }

        node.put("page_rank", sum);
        context.write(key, new Text(gson.toJson(node)));

        context.getCounter(Counter.USERS).increment(1);
    }

    private boolean isNode(String json) {
        boolean isNode = false;

        try {
            double num = Double.parseDouble(json);
        } catch (Exception e) {
            isNode = true;
        }

        return isNode;
    }
}

public class UserPageRankJob {

    private static final int MAX_IT = 20;

    private static Job getJobConf() throws IOException {
        Job job = Job.getInstance(new Configuration());
        job.setMapperClass(UserPageRankMapper.class);
        job.setReducerClass(UserPageRankReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setInputFormatClass(KeyValueTextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        return job;
    }

    private static void iterateUntilConvergence(Path basePath, Path inputPath) throws IOException, ClassNotFoundException, InterruptedException {
        UserPageRankJob pagerankJob = new UserPageRankJob();

        int iteration = 0;

        Job job = getJobConf();

        FileSystem fs = FileSystem.get(job.getConfiguration());

        fs.delete(basePath, true);
        fs.mkdirs(basePath);

        Path outputPath = new Path(basePath, "iteration" + iteration);
        FileInputFormat.setInputPaths(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputPath);

        job.waitForCompletion(false);

        // Perform iterations up to the maximum number
        while (iteration < MAX_IT) {
            iteration++;

            job = getJobConf();

            FileInputFormat.setInputPaths(job, outputPath);
            outputPath = new Path(basePath, "iteration" + iteration);
            FileOutputFormat.setOutputPath(job, outputPath);

            job.waitForCompletion(false);
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Path basePath = new Path(args[0]);
        Path inputPath = new Path(args[1]);
        UserPageRankJob.iterateUntilConvergence(basePath, inputPath);
    }
}
