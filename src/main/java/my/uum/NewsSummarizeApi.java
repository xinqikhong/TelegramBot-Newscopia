package my.uum;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class NewsSummarizeApi {
    private static final String API_KEY = "0ad4f0ca82464d50642a32a99c1e9ca2";

    public static String summarizeText(String txt, int sentences) throws IOException, URISyntaxException {
        // Define the API endpoint
        String url = "https://api.meaningcloud.com/summarization-1.0";

        // Build the payload
        String payload = "key=" + API_KEY + "&sentences=" + sentences;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Create an HttpPost request
            HttpPost request = new HttpPost(url);
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");
            request.setEntity(new StringEntity(payload + "&txt=" + txt));

            // Execute the request and get the response
            HttpResponse response = client.execute(request);

            // Check if the response was successful
            if (response.getStatusLine().getStatusCode() == 200) {
                // Print the response body
                System.out.println("Status code: " + response.getStatusLine().getStatusCode());
                return EntityUtils.toString(response.getEntity());
            } else {
                // Handle error
                System.out.println("Failed to summarize the text. Status code: " + response.getStatusLine().getStatusCode());
                return "Failed to summarize the text.";
            }
        }
    }
}
