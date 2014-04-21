package com.whereismydot.extractors;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;
import twitter4j.Status;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageRankExtractor implements FeatureExtractor {
//    private static String PAGERANK_FILE = "/Users/shaundowling/Google Drive/UCL/IRDM/groupcw/example/pagerank/iteration3/part-r-00000";
private static String PAGERANK_FILE = "s3://";
    private static String AWS_ACCESS = "AKIAIQQS32WOARUW24EA";
    private static String AWS_SECRET = "VuiojERut5LrlutY7/0WxSB2l/9aUduLRpw63Evc";

    private Map<String, Double> pagerankMap = new HashMap<>();

    public PageRankExtractor() {
        BufferedReader reader = null;

        if (PAGERANK_FILE.startsWith("s3://")) {
            AWSCredentials myCredentials = new BasicAWSCredentials(AWS_ACCESS, AWS_SECRET);
            AmazonS3Client s3Client = new AmazonS3Client(myCredentials);
            S3Object object = s3Client.getObject(new GetObjectRequest("sentimentalist", "output/shaundowling/FullPageRank/iteration30/part-r-00000"));
            reader = getReaderFromObject(object);

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

    private BufferedReader getReaderFromObject(S3Object object) {
        BufferedReader reader = null;r

        try {
            InputStreamReader inReader = new InputStreamReader(object.getObjectContent());
            File temp = File.createTempFile("pagerank", ".tmp");
            temp.deleteOnExit();

            FileWriter fw = new FileWriter(temp);

            int c = 0;
            while ((c = inReader.read()) != -1)
                fw.write(c);

            fw.flush();

            reader = new BufferedReader(new InputStreamReader(new FileInputStream(temp)));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return reader;
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
        for (Status tweet : tweets) {

        }

        return null;
    }

    public static void main(String[] args) {
        PageRankExtractor extractor = new PageRankExtractor();

        System.out.println("Parsed file");
    }
}
