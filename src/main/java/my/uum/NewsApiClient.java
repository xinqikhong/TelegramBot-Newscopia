package my.uum;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class NewsApiClient {
    private static final String GNEWS_API_BASE_URL = "https://gnews.io/api/v4/";
    private static final String GNEWS_API_KEY = "e21629eda54ba291263f3cb2b4fd1328";

    public static JSONArray getTopHeadlines() throws IOException {
        String url = GNEWS_API_BASE_URL + "top-headlines?lang=en&apikey=" + GNEWS_API_KEY;
        return sendGetRequest(url);
    }

    public static JSONArray getNewsByCountry(String country) throws IOException {
        String url = GNEWS_API_BASE_URL + "search?q=example&lang=en&country=" + country + "&apikey=" + GNEWS_API_KEY;
        return sendGetRequest(url);
    }

    public static JSONArray getNewsByKeyword(String keyword) throws IOException {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString());
        String url = GNEWS_API_BASE_URL + "search?q=" + encodedKeyword + "&lang=en&apikey=" + GNEWS_API_KEY;
        return sendGetRequest(url);
    }

    private static JSONArray sendGetRequest(String url) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject jsonResponse = new JSONObject(responseBody);
                if (jsonResponse.has("articles")) {
                    return jsonResponse.getJSONArray("articles");
                }
            }
        }
        return new JSONArray();
    }
}

