package ru.tbank.emailcheckerbot.bot.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.emailcheckerbot.bot.command.registration.RegistrationStep;
import ru.tbank.emailcheckerbot.bot.command.registration.RegistrationStepFactory;

@Component
@RequiredArgsConstructor
public class AddEmailCommand implements BotCommand {

    private final RegistrationStepFactory registrationStepFactory;

    @Override
    public SendMessage execute(Update update) {
        if (update.hasMessage()) {
            return registrationStepFactory.getEmailRegistrationStep(RegistrationStep.INITIAL).execute(update);
        }

        RegistrationStep currentStep = RegistrationStep.valueOf(
                update.getCallbackQuery().getData().split(" ")[1]
        );

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
