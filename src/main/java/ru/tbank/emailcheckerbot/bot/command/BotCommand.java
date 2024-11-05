package ru.tbank.emailcheckerbot.bot.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotCommand {
    SendMessage execute(Update update);

    String getName();

    String getDescription();
}
