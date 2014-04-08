package experimentation;

import com.whereismydot.utils.TwitterParser;
import twitter4j.Status;
import twitter4j.TwitterException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MiniDataTest {
    public static void main(String[] args) throws IOException, TwitterException {

        InputStream in = TwitterTest.class.getResourceAsStream("/small_example.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String line;
        while ((line = br.readLine()) != null) {

            Status status = TwitterParser.parseOrNull(line);
            System.out.println(status);

        }
    }
}
