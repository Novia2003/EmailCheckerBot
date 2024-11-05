package ru.tbank.emailcheckerbot.bot.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.emailcheckerbot.service.UserStateService;
import ru.tbank.emailcheckerbot.bot.command.registration.*;

@Component
@RequiredArgsConstructor
public class AddEmailCommand implements BotCommand {

    private final UserStateService userStateService;

    private final InitialEmailRegistrationStep initialEmailRegistrationStep;

    private final ChoosingProviderStep choosingProviderStep;

    private final GettingTokenStep gettingTokenStep;

    private final PermissionConfirmationStep permissionConfirmationStep;

    @Override
    public SendMessage execute(Update update) {
        Long userId = (update.hasCallbackQuery()) ?
                update.getCallbackQuery().getFrom().getId() : update.getMessage().getFrom().getId();
        RegistrationStep step = userStateService.getStep(userId);

        SendMessage response;

        switch (step) {
            case NONE -> response = initialEmailRegistrationStep.execute(update);

            case CHOOSING_PROVIDER -> response = choosingProviderStep.execute(update);

            case WAITING_FOR_TOKEN -> response = gettingTokenStep.execute(update);

            case WAITING_FOR_PERMISSION_CONFIRMATION -> response = permissionConfirmationStep.execute(update);

            default -> throw new IllegalStateException("Unexpected value: " + step);
        }

        return response;
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
