package com.whereismydot.extractors;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.gson.Gson;
import com.whereismydot.utils.SentimentAnalyser;
import experimentation.TwitterTest;
import twitter4j.Status;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PageRankExtractor implements FeatureExtractor {
//    private static String PAGERANK_FILE = "/Users/shaundowling/Google Drive/UCL/IRDM/groupcw/example/pagerank/iteration3/part-r-00000";
    private static String PAGERANK_FILE = "s3";
    private static final String AWS_ACCESS = "AKIAIQQS32WOARUW24EA";
    private static final String AWS_SECRET = "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";
    private final String BUCKET = "sentimentalist";
    private AmazonS3Client s3Client;

    private Map<String, Double> pagerankMap = new HashMap<>();

    public PageRankExtractor() {
        BufferedReader reader = null;

        if (PAGERANK_FILE.startsWith("s3")) {
            AWSCredentials myCredentials = new BasicAWSCredentials(AWS_ACCESS, AWS_SECRET);
            s3Client = new AmazonS3Client(myCredentials);
            reader = getReaderFromS3AndHandleError();

        } else {
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(PAGERANK_FILE))));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }

        if (reader == null)
            return;

        try {
            parseFile(reader);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private BufferedReader getReaderFromS3AndHandleError() {
        BufferedReader reader = null;

        try {
            reader = getReaderFromS3();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return reader;
    }

    private BufferedReader getReaderFromS3() throws IOException {
        List<S3ObjectSummary> results = s3Client.listObjects(BUCKET, "output/shaundowling/FullPageRank/iteration30/").getObjectSummaries();

        File temp = File.createTempFile("pagerank", ".tmp");
        temp.deleteOnExit();

        FileWriter fw = new FileWriter(temp);

        for (S3ObjectSummary summary : results) {
            S3Object object = s3Client.getObject(summary.getBucketName(), summary.getKey());

            InputStreamReader inReader = new InputStreamReader(object.getObjectContent());

            int c;
            while ((c = inReader.read()) != -1)
                fw.write(c);

            fw.flush();
        }

        return new BufferedReader(new InputStreamReader(new FileInputStream(temp)));
    }

    private void parseFile(BufferedReader reader) throws IOException {
        Gson gson = new Gson();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\t");

            String id = parts[0];
            String dict = parts[1];
            Map<String, Double> parsed = gson.fromJson(dict, pagerankMap.getClass());

            pagerankMap.put(id, parsed.get("page_rank"));
        }
    }

    @Override
    public Map<String, Double> extract(List<Status> tweets) {

        Map<String, Double> output = new HashMap<>();
        double totalPageRank = 0.0;
        double weightedSentiment = 0.0;

        SentimentAnalyser analyser = new SentimentAnalyser();

        for (Status tweet : tweets) {
            String userId = "" + tweet.getUser().getId();
            double pagerank = 0.0;
            try {
                pagerank = pagerankMap.get(userId);
            } catch (Exception e) {
            }

            totalPageRank += pagerank;

            int sentiment = analyser.getSentiment(tweet.getText());
            weightedSentiment += sentiment * pagerank;
        }

        output.put("total-page-rank", totalPageRank);
        output.put("page-rank-sentiment", weightedSentiment);

        return output;
    }

    public static void main(String[] args) {
        PageRankExtractor extractor = new PageRankExtractor();
        Status tweet = TwitterTest.getExampleTweet();

        List<Status> tweets = new LinkedList<>();
        tweets.add(tweet);
        extractor.extract(tweets);

        System.out.println("Parsed file");
    }
}
