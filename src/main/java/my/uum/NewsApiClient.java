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

/**
 * A utility class for interacting with the GNews API to fetch news data.
 */
public class NewsApiClient {
    // Base URL of the GNews API
    private static final String GNEWS_API_BASE_URL = "https://gnews.io/api/v4/";
    // API key for accessing the GNews API
    private static final String GNEWS_API_KEY = "e21629eda54ba291263f3cb2b4fd1328";

    // Fetches the top headlines from the GNews API.
    public static JSONArray getTopHeadlines() throws IOException {
        String url = GNEWS_API_BASE_URL + "top-headlines?lang=en&apikey=" + GNEWS_API_KEY;
        return sendGetRequest(url);
    }

    // Fetches news articles from the GNews API based on a specific country.
    public static JSONArray getNewsByCountry(String country) throws IOException {
        String url = GNEWS_API_BASE_URL + "search?q=example&lang=en&country=" + country + "&apikey=" + GNEWS_API_KEY;
        return sendGetRequest(url);
    }

    // Fetches news articles from the GNews API based on a specific keyword.
    public static JSONArray getNewsByKeyword(String keyword) throws IOException {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString());
        String url = GNEWS_API_BASE_URL + "search?q=" + encodedKeyword + "&lang=en&apikey=" + GNEWS_API_KEY;
        return sendGetRequest(url);
    }

    // Sends an HTTP GET request to the specified URL and retrieves the response.
    private static JSONArray sendGetRequest(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    if (jsonResponse.has("articles")) {
                        return jsonResponse.getJSONArray("articles");
                    } else {
                        System.err.println("Invalid response format: Missing 'articles' key");
                    }
                } else {
                    System.err.println("Failed API call. Status code: " + statusCode);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to send HTTP request: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
        return new JSONArray();
    }
}

