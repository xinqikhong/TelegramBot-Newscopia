package my.uum;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MyNewsBot extends TelegramLongPollingBot {
    private final String newsApiKey = "e21629eda54ba291263f3cb2b4fd1328";

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message msg = update.getMessage();
            String text = msg.getText();
            Long chatId = msg.getChatId();


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
