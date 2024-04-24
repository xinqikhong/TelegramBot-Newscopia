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
            // Handle text messages
            Message msg = update.getMessage();
            String text = msg.getText();
            Long chatId = msg.getChatId();

            if (text.equals("/start")) {
                sendText(chatId, "Welcome to MyNewsBot! Use /help to see available commands.");
            } else if (text.equals("/help")) {
                sendText(chatId, "Available commands:\n" +
                        "/headlines - Get latest news headlines\n" +
                        "/category - Search for news by category\n" +
                        "/country - Search for news by country\n" +
                        "/search [keyword] - Search for news by keyword");
            } else if (text.equals("/headlines")) {
                fetchAndSendHeadlines(chatId);
            } else if (text.equals("/more")) {
                fetchAndSendMore(chatId);
            } else if (text.equals("/category")) {
                sendCategoryMenu(chatId);
            } else if (text.equals("/country")) {
                sendCountryMenu(chatId);
            } else if (text.startsWith("/search")) {
                String keyword = text.substring(8);
                searchAndSend(chatId, keyword, 3);
            }
        } else if (update.hasCallbackQuery()) {
            // Handle callback queries
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.startsWith("category_")) {
                String category = callbackData.substring(9); // Extract category from callback data
                searchAndSend(chatId, category, 1);
            } else if (callbackData.startsWith("country_")) {
                String countryCode = callbackData.substring(8); // Extract country code from callback data
                searchAndSend(chatId, countryCode, 2);
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

            sendArticles(chatId, newsToShow);

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
                sendArticles(chatId, newsToShow);
            } else {
                sendText(chatId, "No news to show.");
            }
        }catch (Exception e) {
            sendText(chatId, "An unexpected error occurred while processing your request. Please try again later.");
            e.printStackTrace(); // This will print the stack trace for debugging purposes
        }
    }

    private void sendCategoryMenu(Long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("Search By Category")
                .replyMarkup(createCategoryMenu())
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup createCategoryMenu() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Define category buttons
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder().text("General").callbackData("category_general").build());
        row1.add(InlineKeyboardButton.builder().text("Business").callbackData("category_business").build());
        keyboard.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder().text("Technology").callbackData("category_technology").build());
        row2.add(InlineKeyboardButton.builder().text("Science").callbackData("category_science").build());
        keyboard.add(row2);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(InlineKeyboardButton.builder().text("Sports").callbackData("category_sports").build());
        row3.add(InlineKeyboardButton.builder().text("Entertainment").callbackData("category_entertainment").build());
        keyboard.add(row3);

        markup.setKeyboard(keyboard);
        return markup;
    }

    private void searchAndSend(Long chatId, String search, int identifier) {
        try {
            switch (identifier) {
                case 1:
                    fetchedNews = NewsApiClient.getNewsByCategory(search);
                    break;
                case 2:
                    fetchedNews = NewsApiClient.getNewsByCountry(search);
                    break;
                case 3:
                    fetchedNews = NewsApiClient.getNewsByKeyword(search);
                    break;
                default:
                    fetchedNews = null;
                    break;
            }

            newsToShow = new JSONArray();

            for (int i = 0; i < 3 && i < fetchedNews.length(); i++) {
                newsToShow.put(fetchedNews.getJSONObject(i));
            }

            sendArticles(chatId, newsToShow);

            if (fetchedNews.length() > 3) {
                sendText(chatId, "Type /more to view more news.");
            }
        } catch (IOException e) {
            sendText(chatId, "Failed to fetch news. Please try again later.");
        }
    }

    /*private void fetchAndSendCategory(Long chatId, String category) {
        try {
            fetchedNews = NewsApiClient.getNewsByCategory(category);
            newsToShow  = new JSONArray();

            for (int i = 0; i < 3 && i < fetchedNews.length(); i++) {
                newsToShow.put(fetchedNews.getJSONObject(i));
            }

            sendArticles(chatId, newsToShow);

            if (fetchedNews.length() > 3) {
                sendText(chatId, "Type /more to view more news.");
            }
        } catch (IOException e) {
            sendText(chatId, "Failed to fetch news. Please try again later.");
        }
    }*/

    private void sendCountryMenu(Long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("Search By Country")
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

        /// Define country buttons
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

    /*private void fetchAndSendCountry(Long chatId, String countryCode) {
        try {
            fetchedNews = NewsApiClient.getNewsByCountry(countryCode);
            newsToShow  = new JSONArray();

            for (int i = 0; i < 3 && i < fetchedNews.length(); i++) {
                newsToShow.put(fetchedNews.getJSONObject(i));
            }

            sendArticles(chatId, newsToShow);

            if (fetchedNews.length() > 3) {
                sendText(chatId, "Type /more to view more news.");
            }
        } catch (IOException e) {
            sendText(chatId, "Failed to fetch news. Please try again later.");
        }
    }*/

    /*private void fetchAndSendKeyword(Long chatId, String keyword) {
        // Implement logic to search news by keyword from News API and send them to the user
        try {
            fetchedNews = NewsApiClient.getNewsByKeyword(keyword);
            newsToShow  = new JSONArray();

            for (int i = 0; i < 3 && i < fetchedNews.length(); i++) {
                newsToShow.put(fetchedNews.getJSONObject(i));
            }

            sendArticles(chatId, newsToShow);

            if (fetchedNews.length() > 3) {
                sendText(chatId, "Type /more to view more news.");
            }
        } catch (IOException e) {
            sendText(chatId, "Failed to search news. Please try again later.");
        }
    }*/

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
        if (articles.isEmpty()) {
            sendText(chatId, "No articles found.");
            return;
        }
        for (int i = 0; i < articles.length(); i++) {
            JSONObject article = articles.getJSONObject(i);
            String title = article.getString("title").replaceAll("<b>|</b>", "");
            String url = article.getString("url");
            message.append(title).append("\n").append(url).append("\n\n");
        }
        System.out.println("Message to send: " + message.toString()); // Debug output
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
