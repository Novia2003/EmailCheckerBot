package ru.tbank.emailcheckerbot.bot.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.emailcheckerbot.bot.command.BotCommand;
import ru.tbank.emailcheckerbot.bot.command.Command;
import ru.tbank.emailcheckerbot.bot.command.registration.RegistrationStep;
import ru.tbank.emailcheckerbot.bot.command.registration.factory.RegistrationStepFactory;
import ru.tbank.emailcheckerbot.exeption.InvalidCallbackQueryException;

@Component
@RequiredArgsConstructor
public class AddEmailCommand implements BotCommand {

    private final RegistrationStepFactory registrationStepFactory;

    @Override
    public SendMessage execute(Update update) {
        if (update.hasMessage()) {
            return registrationStepFactory.getEmailRegistrationStep(RegistrationStep.INITIAL).execute(update);
        }

        String[] callbackQueryData = update.getCallbackQuery().getData().split(" ");

        if (callbackQueryData.length < 2) {
            throw new InvalidCallbackQueryException("Incorrect number of words in the callbackQuery");
        }

        RegistrationStep currentStep = RegistrationStep.valueOf(callbackQueryData[1]);

        return registrationStepFactory.getEmailRegistrationStep(currentStep).execute(update);
    }

    @Override
    public String getName() {
        return Command.ADD_EMAIL.getTitle();
    }

    @Override
    public String getDescription() {
        return "команда для добавления нового почтового аккаунта";
    }
}
