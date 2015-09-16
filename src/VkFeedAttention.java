import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class VkFeedAttention {

    private static String USER_ID;
    private static String ACCESS_TOKEN;
    private static HttpClient httpclient = new DefaultHttpClient();
    private static URIBuilder builder = new URIBuilder();
    private static long RUN_TIME;
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy-HH-mm", Locale.ENGLISH);

    public static void main(String[] args) throws URISyntaxException, IOException, ParseException {

        httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
                CookiePolicy.IGNORE_COOKIES);

        List<String> lines = Files.readAllLines(Paths.get(".", "src", "access_token").normalize(), Charset.forName("UTF-8"));
        ACCESS_TOKEN = lines.get(0);
        USER_ID = lines.get(1);

        builder.setScheme("https").setHost("api.vk.com").setPath("/method/friends.get")
                .setParameter("uid", USER_ID)
                .setParameter("fields", "uid")
                .setParameter("access_token", ACCESS_TOKEN);
        URI uri = builder.build();
        HttpGet httpget = new HttpGet(uri);

        PrintWriter writer = new PrintWriter(".//out//" + new Long(System.currentTimeMillis()) + ".txt", "UTF-8");

        RUN_TIME = System.currentTimeMillis();
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            public void run() {
                long time = System.currentTimeMillis();
                int onlineFriends = getOnlineFriends(httpget);
                int lastMinuteNews = getLastMinuteNewes(time / 1000L);
                writeData(time, onlineFriends, lastMinuteNews, writer);
            }
        }, 0, 60 * 1000);
    }

    private static void writeData(long time, int onlineFriends, int lastMinuteNews, PrintWriter writer) {
        String data = time + ", " + onlineFriends + ", " +
                lastMinuteNews + ", " + sdf.format(new Date(System.currentTimeMillis())) + "\n";
        File file = new File(".//out//" + new Long(RUN_TIME) + ".txt");

        try (FileOutputStream fop = new FileOutputStream(file, true)) {
            if (!file.exists()) {
               file.createNewFile();
            }
            byte[] contentInBytes = data.getBytes();
            fop.write(contentInBytes);
            fop.flush();
            fop.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static int getLastMinuteNewes(long unixtime) {
        HttpResponse response = null;
        int newNews = -1;
        try {
            builder.setScheme("https").setHost("api.vk.com").setPath("/method/newsfeed.get")
                    .setParameter("uid", USER_ID)
                    .setParameter("count", "100")
                    .setParameter("filters", "post")
                    .setParameter("start_time", Long.toString(unixtime - 60))
                    .setParameter("end_time", Long.toString(unixtime))
                    .setParameter("access_token", ACCESS_TOKEN);
            final URI uri = builder.build();
            response = httpclient.execute(new HttpGet(uri));
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = null;
                try {
                    instream = entity.getContent();
                    String responseAsString = IOUtils.toString(instream);
                    JSONParser parser = new JSONParser();
                    JSONObject jsonResponse = (JSONObject) parser.parse(responseAsString);
                    JSONObject news = (JSONObject) jsonResponse.get("response");
                    JSONArray items = (JSONArray) news.get("items");
                    newNews = items.size();
                    System.out.println("Time: " + sdf.format(new Date(System.currentTimeMillis())) + "; new news: " + items.size());
                } finally {
                    if (instream != null)
                        instream.close();
                }
            }
        } catch (IOException | ParseException | URISyntaxException | NullPointerException e) {
            e.printStackTrace();
        }
        return newNews;
    }

    private static int getOnlineFriends(HttpGet httpget) {
        HttpResponse response = null;
        int online = -1;
        try {
            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = null;
                try {
                    instream = entity.getContent();
                    String responseAsString = IOUtils.toString(instream);
                    online = parseAndPrint(responseAsString);
                } finally {
                    if (instream != null)
                        instream.close();
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return online;
    }


    private static int parseAndPrint(String resp) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonResponse = (JSONObject) parser.parse(resp);
        JSONArray friendslist = (JSONArray) jsonResponse.get("response");
        int online = 0;
        for (int i = 1; i < friendslist.size(); i++) {
            JSONObject friend = (JSONObject) friendslist.get(i);
            online += (Long) friend.get("online");
        }
        System.out.println("Time: " + sdf.format(new Date(System.currentTimeMillis())) + "; online frds: " + online);
        return online;
    }

}

