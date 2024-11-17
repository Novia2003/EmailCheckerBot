package ru.tbank.emailcheckerbot.bot.command.registration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.emailcheckerbot.service.EmailUIDService;
import ru.tbank.emailcheckerbot.service.UserStateService;

@Component
@RequiredArgsConstructor
public class PermissionConfirmationStep implements EmailRegistrationStep {

    private final UserStateService userStateService;
    private final EmailUIDService emailUIDService;

    @Override
    public SendMessage execute(Update update) {
        Long userId = update.getCallbackQuery().getFrom().getId();
        Long lastMessageUID = emailUIDService.getLastMessageUID(
                userStateService.getEmail(userId),
                userStateService.getEmailProvider(userId),
                userStateService.getAccessToken(userId)
        );

        userStateService.setLastMessageUID(userId, lastMessageUID);
        userStateService.transferInformationDatabase(userId);

        SendMessage message = new SendMessage();
        message.setChatId(update.getCallbackQuery().getMessage().getChatId());
        message.setText("Поздравляем! Ваш аккаунт успешно добавлен для проверки почты.");

        return message;
    }
}

