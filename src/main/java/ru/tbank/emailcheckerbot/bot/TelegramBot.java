package ru.tbank.emailcheckerbot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.emailcheckerbot.bot.command.BotCommand;
import ru.tbank.emailcheckerbot.bot.command.Command;
import ru.tbank.emailcheckerbot.bot.command.factory.CommandFactory;
import ru.tbank.emailcheckerbot.exeption.InvalidCallbackQueryException;

import java.util.NoSuchElementException;

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

        try {
            BotCommand command = commandFactory.getCommand(messageText);
            executeMessage(command.execute(update));
        } catch (NoSuchElementException e) {
            BotCommand commandsList = commandFactory.getCommand(Command.COMMANDS.getTitle());
            executeMessage(commandsList.execute(update));
        }
    }

    private void handleCallbackQuery(Update update) {
        String[] callbackQueryData = update.getCallbackQuery().getData().split(" ");

        if (callbackQueryData.length < 1) {
            throw new InvalidCallbackQueryException("Incorrect number of words in the callbackQuery");
        }

        String commandName = callbackQueryData[0];
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
