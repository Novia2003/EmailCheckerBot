package ru.tbank.emailcheckerbot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.emailcheckerbot.bot.command.BotCommand;
import ru.tbank.emailcheckerbot.bot.command.CommandFactory;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private CommandFactory commandFactory;

    @Value("${telegram.bot.username}")
    private String botUsername;

    public TelegramBot(@Value("${telegram.bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update);
        }
    }

    private void handleMessage(Update update) {
        String messageText = update.getMessage().getText();

        BotCommand command = commandFactory.getCommand(messageText);

        if (command != null) {
            executeMessage(command.execute(update));
        }
    }

    private void handleCallbackQuery(Update update) {
        String data = update.getCallbackQuery().getData();
        String commandName = data.split(" ")[0];
        BotCommand command = commandFactory.getCommand(commandName);

        executeMessage(command.execute(update));
    }

    public void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (Exception e) {
            log.error("Error occurred: {}", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}
