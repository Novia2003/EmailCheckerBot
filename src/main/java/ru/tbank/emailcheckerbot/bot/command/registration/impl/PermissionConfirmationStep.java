package ru.tbank.emailcheckerbot.bot.command.registration.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.emailcheckerbot.bot.command.registration.EmailRegistrationStep;
import ru.tbank.emailcheckerbot.bot.command.registration.RegistrationStep;
import ru.tbank.emailcheckerbot.entity.redis.UserEmailRedisEntity;
import ru.tbank.emailcheckerbot.exeption.EmailAccessException;
import ru.tbank.emailcheckerbot.exeption.UserEmailRedisEntityNotFoundException;
import ru.tbank.emailcheckerbot.service.authentication.AuthenticationService;
import ru.tbank.emailcheckerbot.service.email.EmailUIDService;
import ru.tbank.emailcheckerbot.service.encryption.EncryptionService;
import ru.tbank.emailcheckerbot.service.user.UserEmailRedisService;

import java.time.Instant;

import static ru.tbank.emailcheckerbot.bot.util.TelegramUtils.getUserInactivityMessage;

@Component
@RequiredArgsConstructor
public class PermissionConfirmationStep implements EmailRegistrationStep {

    private final UserEmailRedisService userEmailRedisService;
    private final AuthenticationService authenticationService;
    private final EmailUIDService emailUIDService;
    private final EncryptionService encryptionService;

    @Override
    public SendMessage execute(Update update) {
        Long userId = update.getCallbackQuery().getFrom().getId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        try {
            if (Instant.now().isAfter(userEmailRedisService.getEndAccessTokenLife(userId))) {
                authenticationService.refreshToken(userId, false);
            }

            long lastMessageUID;

            try {
                UserEmailRedisEntity userEmailRedisEntity = userEmailRedisService.getUserEmailRedisEntity(userId);

                lastMessageUID = emailUIDService.getLastMessageUID(
                        userEmailRedisEntity.getEmail(),
                        userEmailRedisEntity.getMailProvider(),
                        encryptionService.decryptToken(userEmailRedisEntity.getAccessToken())
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
        } catch (UserEmailRedisEntityNotFoundException e) {
            return getUserInactivityMessage(chatId);
        }
    }

    @Override
    public RegistrationStep getRegistrationStep() {
        return RegistrationStep.PERMISSION_CONFIRMATION;
    }


    private SendMessage getFailedPermissionConfirmationMessage(Long chatId, String exceptionMessage) {
        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        String responseText = exceptionMessage + "\nПожалуйста, разрешите доступ к почтовому ящику";
        response.setText(responseText);

        return response;
    }
}

