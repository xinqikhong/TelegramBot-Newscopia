package my.uum;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ArticleScraper {
    public static String scrapeArticleText(String articleUrl) throws IOException {
        Document doc = Jsoup.connect(articleUrl).get();
        Elements paragraphs = doc.select("p"); // Assuming the article text is contained within <p> elements
        StringBuilder articleText = new StringBuilder();
        for (Element paragraph : paragraphs) {
            articleText.append(paragraph.text()).append("\n");
        }
        return articleText.toString();
    }
}
