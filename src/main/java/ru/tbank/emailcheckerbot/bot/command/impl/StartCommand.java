package ru.tbank.emailcheckerbot.bot.command.impl;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.emailcheckerbot.bot.command.BotCommand;
import ru.tbank.emailcheckerbot.bot.command.Command;

@Component
public class StartCommand implements BotCommand {

    @Override
    public SendMessage execute(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(
                "Добро пожаловать в EmailCheckerBot!" +
                        " Этот бот позволяет проверять новые сообщения на нескольких почтовых аккаунтах сразу.");

        return sendMessage;
    }

    @Override
    public String getName() {
        return Command.START.getTitle();
    }

    @Override
    public String getDescription() {
        return "команда с описанием предназначения бота";
    }
}
