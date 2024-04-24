package my.uum;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

public class MyNewsBot extends TelegramLongPollingBot {
    private final String newsApiKey = "e21629eda54ba291263f3cb2b4fd1328";

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message msg = update.getMessage();
            String text = msg.getText();
            Long chatId = msg.getChatId();

            if (text.equals("/start")) {
                sendText(chatId, "Welcome to MyNewsBot! Use /help to see available commands.");
            } else if (text.equals("/help")) {
                sendText(chatId, "Available commands:\n" +
                        "/headlines - Get latest news headlines\n" +
                        "/sports - Get sports news\n" +
                        "/technology - Get technology news\n" +
                        "/search [keyword] - Search for news by keyword");
            } else if (text.equals("/headlines")) {
                fetchAndSendHeadlines(chatId);
            } else if (text.equals("/sports")) {
                fetchAndSendNewsByCategory(chatId, "sports");
            } else if (text.equals("/technology")) {
                fetchAndSendNewsByCategory(chatId, "technology");
            } else if (text.startsWith("/search")) {
                String keyword = text.substring(8);
                searchAndSendNewsByKeyword(chatId, keyword);
            }
        }
    }

    private void fetchAndSendHeadlines(Long chatId) {
        // Implement logic to fetch headlines from News API and send them to the user
        try {
            JSONArray headlines = NewsApiClient.getTopHeadlines();
            sendArticles(chatId, headlines);
        } catch (IOException e) {
            sendText(chatId, "Failed to fetch headlines. Please try again later.");
        }
    }

    private void fetchAndSendNewsByCategory(Long chatId, String category) {
        // Implement logic to fetch news by category from News API and send them to the user
        try {
            JSONArray news = NewsApiClient.getNewsByCategory(category);
            sendArticles(chatId, news);
        } catch (IOException e) {
            sendText(chatId, "Failed to fetch news. Please try again later.");
        }
    }

    private void searchAndSendNewsByKeyword(Long chatId, String keyword) {
        // Implement logic to search news by keyword from News API and send them to the user
        try {
            JSONArray news = NewsApiClient.searchNewsByKeyword(keyword);
            sendArticles(chatId, news);
        } catch (IOException e) {
            sendText(chatId, "Failed to search news. Please try again later.");
        }
    }

    private void sendText(Long chatId, String text) {
        //Send Welcome Msg when user enter command /start
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString()) //Who are we sending a message to
                .text(text).build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendArticles(Long chatId, JSONArray articles) {
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < articles.length(); i++) {
            JSONObject article = articles.getJSONObject(i);
            String title = article.getString("title");
            String url = article.getString("url");
            message.append("<b>").append(title).append("</b>\n").append(url).append("\n\n");
        }
        sendText(chatId, message.toString());
    }

    @Override
    public String getBotUsername() {
        // Return bot username
        return "s286130_bot";
    }

    @Override
    public String getBotToken() {
        // Return bot token
        return "6580203514:AAE2xDxJslQ_aiv6WYLubhriAj7t8wDVxwg";
    }
}
