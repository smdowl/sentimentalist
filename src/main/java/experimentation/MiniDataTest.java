package experimentation;

import com.whereismydot.dataobjects.AugStatus;
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

            AugStatus status = new AugStatus(line);
            System.out.println(status);

        }
    }
}
