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
 * This class is for interacting with the GNews API to fetch news data.
 *
 * @author Khong Xin Qi
 */
public class NewsApiClient {
    // Base URL of the GNews API
    private static final String GNEWS_API_BASE_URL = "https://gnews.io/api/v4/";

    // API key for accessing the GNews API
    private static String apiKey = System.getenv("API_KEY");

    /**
     * This method is for fetching the top headlines from the GNews API.
     * @return A JSONArray containing the top headlines.
     * @throws IOException If an IO error occurs during the API call.
     */
    public static JSONArray getTopHeadlines() throws IOException {
        String url = GNEWS_API_BASE_URL + "top-headlines?lang=en&apikey=" + apiKey;
        return sendGetRequest(url);
    }

    /**
     * This method is for fetching news articles from the GNews API based on a specific country.
     * @param country The country for which news articles are to be fetched.
     * @return A JSONArray containing news articles related to the specified country.
     * @throws IOException If an IO error occurs during the API call.
     */
    public static JSONArray getNewsByCountry(String country) throws IOException {
        String url = GNEWS_API_BASE_URL + "search?q=example&lang=en&country=" + country + "&apikey=" + apiKey;
        return sendGetRequest(url);
    }

    /**
     * This method is for fetching news articles from the GNews API based on a specific keyword.
     * @param keyword The keyword for which news articles are to be fetched.
     * @return A JSONArray containing news articles related to the specified keyword.
     * @throws IOException If an IO error occurs during the API call.
     */
    public static JSONArray getNewsByKeyword(String keyword) throws IOException {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString());
        String url = GNEWS_API_BASE_URL + "search?q=" + encodedKeyword + "&lang=en&apikey=" + apiKey;
        return sendGetRequest(url);
    }

    /**
     * This method is for sending an HTTP GET request to the specified URL and retrieving the response.
     * @param url The URL to which the GET request is sent.
     * @return A JSONArray containing the response data.
     */
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

