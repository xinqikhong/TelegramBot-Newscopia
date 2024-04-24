package my.uum;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class MyNewsBot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        // Handle incoming updates here
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
