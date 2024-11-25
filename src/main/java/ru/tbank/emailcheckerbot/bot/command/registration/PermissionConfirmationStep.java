package ru.tbank.emailcheckerbot.bot.command.registration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.emailcheckerbot.bot.command.Command;
import ru.tbank.emailcheckerbot.exeption.EmailAccessException;
import ru.tbank.emailcheckerbot.service.AuthenticationService;
import ru.tbank.emailcheckerbot.service.EmailUIDService;
import ru.tbank.emailcheckerbot.service.UserEmailRedisService;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class PermissionConfirmationStep implements EmailRegistrationStep {

    private final UserEmailRedisService userEmailRedisService;
    private final AuthenticationService authenticationService;
    private final EmailUIDService emailUIDService;

    @Override
    public SendMessage execute(Update update) {
        Long userId = update.getCallbackQuery().getFrom().getId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (userEmailRedisService.isUserEmailRecordNotExists(userId)) {
            return getUserInactivityMessage(chatId);
        }

        if (Instant.now().isAfter(userEmailRedisService.getEndAccessTokenLife(userId))) {
            authenticationService.refreshToken(userId, false);
        }

        long lastMessageUID;

        try {
            lastMessageUID = emailUIDService.getLastMessageUID(
                    userEmailRedisService.getEmail(userId),
                    userEmailRedisService.getMailProvider(userId),
                    userEmailRedisService.getAccessToken(userId)
            );
        } catch (EmailAccessException e) {
            return getFailedPermissionConfirmationMessage(chatId, e.getMessage());
        }

        userEmailRedisService.setLastMessageUID(userId, lastMessageUID);
        userEmailRedisService.transferEntityFromRedisToJpa(userId);

        SendMessage message = new SendMessage();
        message.setChatId(update.getCallbackQuery().getMessage().getChatId());
        message.setText("Поздравляем! Ваш аккаунт успешно добавлен для проверки почты.");

        return message;
    }

    @Override
    public RegistrationStep getRegistrationStep() {
        return RegistrationStep.PERMISSION_CONFIRMATION;
    }

    private SendMessage getUserInactivityMessage(Long chatId) {
        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        String responseText = "Вы долго бездействовали, и запись о Вас была удалена.\n" +
                "Пожалуйста, начните процесс добавления почты с начала " + Command.ADD_EMAIL.getTitle();
        response.setText(responseText);

        return response;
    }

    private SendMessage getFailedPermissionConfirmationMessage(Long chatId, String exceptionMessage) {
        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        String responseText = exceptionMessage + "\nПожалуйста, разрешите доступ к почтовому ящику";
        response.setText(responseText);

        return response;
    }
}

