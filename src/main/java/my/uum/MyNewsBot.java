package my.uum;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

/**
 * MyNewsBot is a Telegram bot that provides news headlines, country-based news, and keyword-based news search.
 * It interacts with users via Telegram messages and inline keyboard options.
 */
public class MyNewsBot extends TelegramLongPollingBot {
    // JSONArray to store fetched news articles
    private JSONArray fetchedNews = null;
    // JSONArray to store news articles to show to users
    private JSONArray newsToShow = null;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message msg = update.getMessage();
            String text = msg.getText();
            Long chatId = msg.getChatId();

            if (text.equals("/start")) {
                // Send welcome message when user enters /start command
                sendText(chatId, "Welcome to MyNewsBot! Use /help to see available commands.");
            } else if (text.equals("/help")) {
                // Provide help information to user
                sendText(chatId, "Available commands:\n" +
                        "/help - See available commands\n" +
                        "/headlines - Get latest news headlines\n" +
                        "/country - Search for news by country\n" +
                        "/search [keyword] - Search for news by keyword (e.g. /search malaysia)");
            } else if (text.equals("/headlines")) {
                // Fetch and send latest news headlines
                fetchAndSendHeadlines(chatId);
            } else if (text.equals("/more")) {
                // Fetch and send more news articles
                fetchAndSendMore(chatId);
            } else if (text.equals("/country")) {
                // Display country menu for news search
                sendCountryMenu(chatId);
            } else if (text.startsWith("/search")) {
                if (text.length() <= 7) {
                    // Prompt user to provide keyword for search
                    sendText(chatId, "Please type the keyword after the /search command. (e.g. /search malaysia)");
                } else {
                    // Perform news search based on provided keyword
                    String keyword = text.substring(8);
                    Search(chatId, keyword, 2);
                }
            } else{
                // Default response for unrecognized commands
                sendText(chatId, "Welcome to MyNewsBot! MyNewsBot offers top headlines, country & keyword search. Get global news updates fast! Use /help to see available commands.");
            }
        } else if (update.hasCallbackQuery()) {
            // Handle callback queries (e.g., when user selects country from inline keyboard)
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();
            Long chatId = callbackQuery.getMessage().getChatId();

            if (callbackData.startsWith("country_")) {
                // Perform news search based on selected country
                String countryCode = callbackData.substring(8);
                Search(chatId, countryCode, 1);
            }
            // Close the callback query after processing
            AnswerCallbackQuery close = AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId()).build();
            try {
                execute(close);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Method to fetch and send latest news headlines
    private void fetchAndSendHeadlines(Long chatId) {
        try {
            fetchedNews = NewsApiClient.getTopHeadlines();
            if (fetchedNews != null) {
                newsToShow = new JSONArray();
                // Limit to first 3 headlines for display
                for (int i = 0; i < 3 && i < fetchedNews.length(); i++) {
                    newsToShow.put(fetchedNews.getJSONObject(i));
                }
                // Send the fetched headlines to the user
                sendArticles(chatId, newsToShow, "Top Headlines:");
                if (fetchedNews.length() > 3) {
                    // Prompt user to get more headlines if available
                    sendText(chatId, "Type /more to view more headlines.");
                }
            } else {
                // Inform user if fetching headlines fails
                sendText(chatId, "Failed to fetch headlines. Please try again later.");
            }
        } catch (IOException e) {
            // Handle IO exception
            sendText(chatId, "Failed to fetch headlines. Please try again later.");
            e.printStackTrace();
        }
    }

    // Method to fetch and send more news articles
    private void fetchAndSendMore(Long chatId) {
        try {
            if (fetchedNews != null) {
                if (newsToShow.length() < 7) {
                    newsToShow = new JSONArray();
                    // Retrieve additional articles beyond the initial headlines
                    for (int i = 3; i < fetchedNews.length(); i++) {
                        newsToShow.put(fetchedNews.getJSONObject(i));
                    }
                    // Send the additional articles to the user
                    sendArticles(chatId, newsToShow, "More:");
                } else{
                    // Prompt user to search articles by other cmd if they try to enter /more cmd more than once
                    sendText(chatId, "No more articles found. Please get more news by using commands:\n" +
                            "/headlines - Get latest news headlines\n" +
                            "/country - Search for news by country\n" +
                            "/search - Search for news by keyword (e.g. /search malaysia)");
                }
            } else {
                // Inform user if no more articles available
                sendText(chatId, "No more articles found.");
            }
        }catch (Exception e) {
            // Handle any unexpected exceptions
            sendText(chatId, "An unexpected error occurred while processing your request. Please try again later.");
            e.printStackTrace();
        }
    }

    // Method to perform news search by country or keyword
    private void Search(Long chatId, String searchText, int identifier) {
        try {
            String context = "";
            switch (identifier) {
                case 1:
                    // Search news by country
                    fetchedNews = NewsApiClient.getNewsByCountry(searchText);
                    String countryName = getCountryName(searchText);
                    context = "Country: " + countryName;
                    break;
                case 2:
                    // Search news by keyword
                    fetchedNews = NewsApiClient.getNewsByKeyword(searchText);
                    context = "Keyword: " + searchText;
                    break;
                default:
                    fetchedNews = null;
                    break;
            }

            if (fetchedNews != null) {
                newsToShow = new JSONArray();
                // Limit to first 3 articles for display
                for (int i = 0; i < 3 && i < fetchedNews.length(); i++) {
                    newsToShow.put(fetchedNews.getJSONObject(i));
                }
                // Send the search results to the user
                sendArticles(chatId, newsToShow, context);

                if (fetchedNews.length() > 3) {
                    // Prompt user to get more news if available
                    sendText(chatId, "Type /more to view more news.");
                }
            } else {
                // Inform user if fetching news fails
                sendText(chatId, "Failed to fetch news. Please try again later.");
            }
        } catch (IOException e) {
            // Handle IO exception
            sendText(chatId, "Failed to fetch news. Please try again later.");
        }
    }

    // Method to send country menu for news search
    private void sendCountryMenu(Long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("Search By Country:")
                .replyMarkup(createCountryMenu())
                .build();
        try {
            // Send the country menu to the user
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Method to create inline keyboard menu for selecting country
    private InlineKeyboardMarkup createCountryMenu() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder().text("United States").callbackData("country_us").build());
        row1.add(InlineKeyboardButton.builder().text("United Kingdom").callbackData("country_gb").build());
        keyboard.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder().text("Australia").callbackData("country_au").build());
        row2.add(InlineKeyboardButton.builder().text("Switzerland").callbackData("country_ch").build());
        keyboard.add(row2);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(InlineKeyboardButton.builder().text("Singapore").callbackData("country_sg").build());
        row3.add(InlineKeyboardButton.builder().text("Philippines").callbackData("country_ph").build());
        keyboard.add(row3);

        markup.setKeyboard(keyboard);
        return markup;
    }

    private void sendText(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text).build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendArticles(Long chatId, JSONArray articles, String context) {
        StringBuilder message = new StringBuilder();

        message.append(context).append("\n\n");
        if (articles.isEmpty()) {
            sendText(chatId, "No articles found.");
            return;
        }
        for (int i = 0; i < articles.length(); i++) {
            JSONObject article = articles.getJSONObject(i);
            String title = article.getString("title").replaceAll("<b>|</b>", "");
            String url = article.getString("url");
            message.append((i + 1) + ". ").append(title).append("\n").append(url).append("\n\n");
        }
        sendText(chatId, message.toString());
    }

    private String getCountryName(String countryCode) {
        switch (countryCode) {
            case "us":
                return "United States";
            case "gb":
                return "United Kingdom";
            case "au":
                return "Australia";
            case "ch":
                return "Switzerland";
            case "sg":
                return "Singapore";
            case "ph":
                return "Philippines";
            default:
                return countryCode;
        }
    }

    @Override
    public String getBotUsername() {
        return "s286130_bot";
    }

    @Override
    public String getBotToken() {
        return "6580203514:AAE2xDxJslQ_aiv6WYLubhriAj7t8wDVxwg";
    }
}