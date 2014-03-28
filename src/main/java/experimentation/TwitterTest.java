package experimentation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import twitter4j.Status;
import twitter4j.TwitterObjectFactory;

import java.io.InputStream;
import java.io.InputStreamReader;

public class TwitterTest {

    public static void main(String[] args) throws Exception {

        JsonParser parser = new JsonParser();
        Gson gson = new Gson();

        InputStream in = TwitterTest.class.getResourceAsStream("/example_tweet.json");
        JsonReader reader = new JsonReader(new InputStreamReader(in));

        JsonElement elem =  parser.parse(reader);
        String cleanJson = gson.toJson(elem);

        Status tweet = TwitterObjectFactory.createStatus(cleanJson);
        System.out.println(tweet);
    }
}