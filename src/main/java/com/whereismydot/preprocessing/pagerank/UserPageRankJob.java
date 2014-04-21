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
    INPUT_USERS,
    USERS,
    LOST_MASS,
    TOTAL_MASS,
    NEW_NODE,
    HANGING_NODE
}

class UserPageRankMapper extends Mapper<Text, Text, Text, Text> {

    @Override
    public void map(Text userId, Text value, Context context)
            throws IOException, InterruptedException {

        Map<String, Object> node = Utils.parseNode(value.toString());

        List<String> adjacencyList = (List<String>) node.get("adjacency");

        context.write(userId, value);

        // If we have no connections then this mass is lost.
        if (adjacencyList.size() == 0) {
            long lostMass = Utils.convertMass(getPageRank(node));
            context.getCounter(Counter.LOST_MASS).increment(lostMass);
            context.getCounter(Counter.HANGING_NODE).increment(1);
        } else {
            Double p = getPageRank(node) / adjacencyList.size();
            for (String otherId : adjacencyList) {
                context.write(new Text(otherId), new Text(p.toString()));
            }
        }


        context.getCounter(Counter.INPUT_USERS).increment(1);
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

        // If a node doesn't exist then we have a new node that does not point to anything (hanging node)
        if (node == null) {
            node = new HashMap<>();
            node.put("adjacency", new LinkedList<String>());
            context.getCounter(Counter.NEW_NODE).increment(1);
        }

        // Add any mass that was lost due to hanging nodes.
        long lostMass = context.getConfiguration().getLong(Utils.LOST_MASS, 0l);
        long userCount = context.getConfiguration().getLong(Utils.USER_COUNT, 1l);

        double actualLost = Utils.convertMass(lostMass);
        sum += actualLost / userCount;

        node.put("page_rank", sum);
        context.write(key, new Text(gson.toJson(node)));

        context.getCounter(Counter.USERS).increment(1);

        long scaledMass = Utils.convertMass(sum);
        context.getCounter(Counter.TOTAL_MASS).increment(scaledMass);
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

    private static final int MAX_IT = 30;

    private Path basePath;
    private Path inputPath;
    private Path outputPath;

    private int iteration = 0;
    private long lostMass = 0l;
    private long userCount = 1l;

    private Job job;

    UserPageRankJob(Path basePath, Path inputPath) throws IOException {
        this.basePath = basePath;
        this.inputPath = inputPath;

        removeOldOutput();
    }

    private void removeOldOutput() throws IOException {
//        FileSystem fs = this.basePath.getFileSystem(new Configuration());
//        fs.delete(basePath, true);
//        fs.mkdirs(basePath);
    }

    public void iterateUntilConvergence() throws IOException, ClassNotFoundException, InterruptedException {
        // Perform iterations up to the maximum number
        while (iteration <= MAX_IT) {
            performIteration();
        }
    }

    private void performIteration() throws IOException, ClassNotFoundException, InterruptedException {
        initJob();

        job.waitForCompletion(false);

        updateStats();
    }

    private void initJob() throws IOException {
        job = Job.getInstance(new Configuration());
        job.setJarByClass(InDegreeJob.class);
        job.setMapperClass(UserPageRankMapper.class);
        job.setReducerClass(UserPageRankReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setInputFormatClass(KeyValueTextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.getConfiguration().setLong(Utils.LOST_MASS, lostMass);
        job.getConfiguration().setLong(Utils.USER_COUNT, userCount);

        updatePaths();
    }

    private void updatePaths() throws IOException {
        outputPath = new Path(basePath, "iteration" + iteration);

        FileInputFormat.setInputPaths(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputPath);
    }

    private void updateStats() {
        inputPath = outputPath;
        iteration++;
        try {
            lostMass = job.getCounters().findCounter(Counter.LOST_MASS).getValue();
            double actualMass = Utils.convertMass(lostMass);

            userCount = job.getCounters().findCounter(Counter.USERS).getValue();

            long totalMass = job.getCounters().findCounter(Counter.TOTAL_MASS).getValue();
            double totalActualMass = Utils.convertMass(totalMass) + actualMass;

            long inputUsers = job.getCounters().findCounter(Counter.INPUT_USERS).getValue();
            long hangingNodes = job.getCounters().findCounter(Counter.HANGING_NODE).getValue();

            System.err.println(actualMass + " mass lost.");
            System.err.println(totalActualMass  + " total mass.");
            System.err.println(inputUsers + " users in.");
            System.err.println(userCount  + " users out.");
            System.err.println(hangingNodes  + " hanging nodes.");
            System.err.println();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Path inputPath = new Path(args[0]);
        Path basePath = new Path(args[1]);

        UserPageRankJob pagerankJob = new UserPageRankJob(basePath, inputPath);
        pagerankJob.iterateUntilConvergence();
    }
}
