package ru.tbank.emailcheckerbot.bot.command.registration.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.tbank.emailcheckerbot.bot.command.registration.RegistrationStep;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.entity.redis.UserEmailRedisEntity;
import ru.tbank.emailcheckerbot.exeption.EmailAccessException;
import ru.tbank.emailcheckerbot.exeption.UserEmailRedisEntityNotFoundException;
import ru.tbank.emailcheckerbot.service.authentication.AuthenticationService;
import ru.tbank.emailcheckerbot.service.email.EmailUIDService;
import ru.tbank.emailcheckerbot.service.encryption.EncryptionService;
import ru.tbank.emailcheckerbot.service.user.UserEmailRedisService;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.tbank.emailcheckerbot.bot.util.TelegramUtils.getUserInactivityMessage;

@ExtendWith(MockitoExtension.class)
class PermissionConfirmationStepTest {

    @Mock
    private UserEmailRedisService userEmailRedisService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private EmailUIDService emailUIDService;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private PermissionConfirmationStep permissionConfirmationStep;

    @Test
    void execute_shouldRefreshTokenAndReturnSuccessMessage() {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(67890L);
        message.setChat(chat);
        User user = new User();
        user.setId(12345L);
        callbackQuery.setFrom(user);
        callbackQuery.setMessage(message);
        update.setCallbackQuery(callbackQuery);

        UserEmailRedisEntity userEmailRedisEntity = new UserEmailRedisEntity();
        userEmailRedisEntity.setEmail("test@example.com");
        userEmailRedisEntity.setAccessToken(new byte[]{});
        userEmailRedisEntity.setMailProvider(MailProvider.YANDEX);
        String token = "token";

        when(userEmailRedisService.getEndAccessTokenLife(user.getId())).thenReturn(Instant.now().minusSeconds(3600));
        when(userEmailRedisService.getUserEmailRedisEntity(user.getId())).thenReturn(userEmailRedisEntity);
        when(encryptionService.decryptToken(userEmailRedisEntity.getAccessToken())).thenReturn(token);
        when(emailUIDService.getLastMessageUID(
                userEmailRedisEntity.getEmail(),
                userEmailRedisEntity.getMailProvider(),
                token
        )).thenReturn(100L);

        SendMessage result = permissionConfirmationStep.execute(update);

        assertEquals(Long.toString(chat.getId()), result.getChatId());
        assertEquals("Поздравляем! Ваш аккаунт успешно добавлен для проверки почты.", result.getText());

        verify(authenticationService).refreshToken(user.getId(), false);
        verify(userEmailRedisService).setLastMessageUID(user.getId(), 100L);
        verify(userEmailRedisService).transferEntityFromRedisToJpa(user.getId());
    }

    @Test
    void execute_shouldReturnFailedPermissionConfirmationMessageWhenEmailAccessException() {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(67890L);
        message.setChat(chat);
        User user = new User();
        user.setId(12345L);
        callbackQuery.setFrom(user);
        callbackQuery.setMessage(message);
        update.setCallbackQuery(callbackQuery);

        when(userEmailRedisService.getEndAccessTokenLife(user.getId())).thenReturn(Instant.now().plusSeconds(3600));
        when(userEmailRedisService.getUserEmailRedisEntity(user.getId()))
                .thenThrow(new EmailAccessException("Доступ запрещён"));

        SendMessage result = permissionConfirmationStep.execute(update);

        assertEquals(Long.toString(chat.getId()), result.getChatId());
        assertEquals("Доступ запрещён\nПожалуйста, разрешите доступ к почтовому ящику", result.getText());
    }

    @Test
    void execute_shouldReturnUserInactivityMessageWhenUserEmailRedisEntityNotFound() {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(67890L);
        message.setChat(chat);
        User user = new User();
        user.setId(12345L);
        callbackQuery.setFrom(user);
        callbackQuery.setMessage(message);
        update.setCallbackQuery(callbackQuery);

        when(userEmailRedisService.getEndAccessTokenLife(user.getId()))
                .thenThrow(new UserEmailRedisEntityNotFoundException("Entity not found"));

        SendMessage result = permissionConfirmationStep.execute(update);

        assertEquals(getUserInactivityMessage(chat.getId()), result);
    }

    @Test
    void getRegistrationStep_shouldReturnPermissionConfirmationStep() {
        RegistrationStep result = permissionConfirmationStep.getRegistrationStep();

        assertEquals(RegistrationStep.PERMISSION_CONFIRMATION, result);
    }
}
