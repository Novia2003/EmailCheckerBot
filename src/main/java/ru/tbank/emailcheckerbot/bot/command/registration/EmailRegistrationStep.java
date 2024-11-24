package ru.tbank.emailcheckerbot.bot.command.registration;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface EmailRegistrationStep {
    SendMessage execute(Update update);

    RegistrationStep getRegistrationStep();
}
