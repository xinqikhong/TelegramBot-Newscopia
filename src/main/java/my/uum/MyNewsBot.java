package my.uum;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class MyNewsBot extends TelegramLongPollingBot {
    private JSONArray fetchedNews = null;
    private JSONArray newsToShow = null;

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
                        "/help - See available commands\n" +
                        "/headlines - Get latest news headlines\n" +
                        "/country - Search for news by country\n" +
                        "/search [keyword] - Search for news by keyword");
            } else if (text.equals("/headlines")) {
                fetchAndSendHeadlines(chatId);
            } else if (text.equals("/more")) {
                fetchAndSendMore(chatId);
            } else if (text.equals("/country")) {
                sendCountryMenu(chatId);
            } else if (text.startsWith("/search")) {
                if (text.length() <= 7) {
                    sendText(chatId, "Please type the keyword after the /search command. (e.g. /search news)");
                } else {
                    String keyword = text.substring(8);
                    Search(chatId, keyword, 2);
                }
            }
        } else if (update.hasCallbackQuery()) {
            // Handle callback queries
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.startsWith("country_")) {
                String countryCode = callbackData.substring(8);
                Search(chatId, countryCode, 1);
            }
        }
    }

    private void fetchAndSendHeadlines(Long chatId) {
        try {
            fetchedNews = NewsApiClient.getTopHeadlines();
            newsToShow  = new JSONArray();

            for (int i = 0; i < 3 && i < fetchedNews.length(); i++) {
                newsToShow.put(fetchedNews.getJSONObject(i));
            }

            sendArticles(chatId, newsToShow, "Top Headlines:");

            if (fetchedNews.length() > 3) {
                sendText(chatId, "Type /more to view more headlines.");
            }
        } catch (IOException e) {
            sendText(chatId, "Failed to fetch headlines. Please try again later.");
        }
    }

    private void fetchAndSendMore(Long chatId) {
        try {
            if (fetchedNews != null) {
                newsToShow = new JSONArray();

                for (int i = 3; i < fetchedNews.length(); i++) {
                    newsToShow.put(fetchedNews.getJSONObject(i));
                }
                sendArticles(chatId, newsToShow, "More:");
            } else {
                sendText(chatId, "No more articles found.");
            }
        }catch (Exception e) {
            sendText(chatId, "An unexpected error occurred while processing your request. Please try again later.");
            e.printStackTrace();
        }
    }

    private void Search(Long chatId, String searchText, int identifier) {
        try {
            String context = "";
            switch (identifier) {
                case 1:
                    fetchedNews = NewsApiClient.getNewsByCountry(searchText);
                    String countryName = getCountryName(searchText);
                    context = "Country: " + countryName;
                    System.out.println(context);
                    break;
                case 2:
                    fetchedNews = NewsApiClient.getNewsByKeyword(searchText);
                    context = "Keyword: " + searchText;
                    break;
                default:
                    fetchedNews = null;
                    break;
            }

            newsToShow = new JSONArray();

            for (int i = 0; i < 3 && i < fetchedNews.length(); i++) {
                newsToShow.put(fetchedNews.getJSONObject(i));
            }

            sendArticles(chatId, newsToShow, context);

            if (fetchedNews.length() > 3) {
                sendText(chatId, "Type /more to view more news.");
            }
        } catch (IOException e) {
            sendText(chatId, "Failed to fetch news. Please try again later.");
        }
    }

    private void sendCountryMenu(Long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("Search By Country:")
                .replyMarkup(createCountryMenu())
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

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
        //Send welcome msg when user enter command /start
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
                return countryCode; // If country code is not found, return the code itself
        }
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
