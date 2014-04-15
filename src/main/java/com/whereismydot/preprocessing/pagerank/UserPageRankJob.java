package com.whereismydot.preprocessing.pagerank;

import com.google.gson.Gson;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.*;

public class UserPageRankJob extends MapReduceBase implements
        Mapper<Text, Text, Text, Text>,
        Reducer<Text, Text, Text, Text> {

    private Gson gson = new Gson();

    public static enum Counter {
        USERS,
        LOST_MASS
    }

    private static final int MAX_IT = 20;
    private static final double counterScale = 1e6;

    @Override
    public void configure(JobConf conf) {
    }

    @Override
    public void map(Text userId, Text value, OutputCollector<Text, Text> out, Reporter reporter)
            throws IOException {

        Map<String, Object> node = parseNode(value.toString());

        List<String> adjacencyList = (List<String>) node.get("adjacency");
        Double p = getPageRank(node) / adjacencyList.size();

        out.collect(userId, value);

        for (String otherId : adjacencyList) {
            out.collect(new Text(otherId), new Text(p.toString()));
        }

        if (adjacencyList.size() == 0) {
            long lostMass = (long) (getPageRank(node) * counterScale);
            reporter.getCounter(Counter.LOST_MASS).increment(lostMass);
        }
    }

    private double getPageRank(Map<String, Object> node) {
        return (double) node.get("page_rank");
    }

    @Override
    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> out,
                       Reporter reporter) throws IOException {

        Map<String, Object> node = null;
        Double sum = 0.0;

        while (values.hasNext()) {
            String next = values.next().toString();

            if (isNode(next))
                node = parseNode(next);
            else
                sum += Double.parseDouble(next);
        }

        if (node == null) {
            node = new HashMap<>();
            node.put("adjacency", new LinkedList<String>());
        }

        node.put("page_rank", sum);
        out.collect(key, new Text(gson.toJson(node)));

        reporter.getCounter(Counter.USERS).increment(1);
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

    private Map<String, Object> parseNode(String json) {
        HashMap<String, Object> node = new HashMap<>();
        node = gson.fromJson(json, node.getClass());
        return node;
    }

    private static JobConf getJobConf() {
        JobConf job = new JobConf(UserPageRankJob.class);
        job.setMapperClass(UserPageRankJob.class);
        job.setReducerClass(UserPageRankJob.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setInputFormat(KeyValueTextInputFormat.class);
        job.setOutputFormat(TextOutputFormat.class);

        return job;
    }

    public static void main(String[] args) throws IOException {

        int iteration = 0;

        JobConf job = getJobConf();

        Path basePath = new Path(args[1]);
        FileSystem fs = FileSystem.get(job);

        fs.delete(basePath, true);
        fs.mkdirs(basePath);

        Path inputPath = new Path(args[0]);
        Path outputPath = new Path(basePath, "iteration" + iteration);
        FileInputFormat.setInputPaths(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputPath);

        JobClient.runJob(job);

        // Perform iterations up to the maximum number
        while (iteration < MAX_IT) {
            iteration++;

            job = getJobConf();

            FileInputFormat.setInputPaths(job, outputPath);
            outputPath = new Path(basePath, "iteration" + iteration);
            FileOutputFormat.setOutputPath(job, outputPath);

            JobClient.runJob(job);
        }
    }
}
