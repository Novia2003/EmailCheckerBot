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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.tbank.emailcheckerbot.bot.command.registration.RegistrationStep;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.exeption.UserEmailRedisEntityNotFoundException;
import ru.tbank.emailcheckerbot.service.provider.MailService;
import ru.tbank.emailcheckerbot.service.provider.factory.MailServiceFactory;
import ru.tbank.emailcheckerbot.service.user.UserEmailRedisService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static ru.tbank.emailcheckerbot.bot.util.TelegramUtils.getUserInactivityMessage;

@ExtendWith(MockitoExtension.class)
class GettingTokenStepTest {

    @Mock
    private MailServiceFactory mailServiceFactory;

    @Mock
    private UserEmailRedisService userEmailRedisService;

    @Mock
    private MailService mailService;

    @InjectMocks
    private GettingTokenStep gettingTokenStep;

    @Test
    void execute_shouldReturnFailedOauthMessageWhenAccessTokenIsNull() {
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

        when(userEmailRedisService.getAccessToken(user.getId())).thenReturn(null);

        SendMessage result = gettingTokenStep.execute(update);

        assertEquals(Long.toString(chat.getId()), result.getChatId());
        assertEquals("Токен доступа не был получен.\n" +
                "Пожалуйста, повторите попытку авторизации", result.getText());
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

        when(userEmailRedisService.getAccessToken(user.getId())).thenThrow(new UserEmailRedisEntityNotFoundException("Entity not found"));

        SendMessage result = gettingTokenStep.execute(update);

        assertEquals(getUserInactivityMessage(chat.getId()), result);
    }

    @Test
    void execute_shouldReturnProviderChoiceMessage() {
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

        when(userEmailRedisService.getAccessToken(user.getId())).thenReturn("accessToken");
        when(userEmailRedisService.getMailProvider(user.getId())).thenReturn(MailProvider.YANDEX);
        when(mailServiceFactory.getService(MailProvider.YANDEX)).thenReturn(mailService);
        when(mailService.getInstructionForPermission()).thenReturn("Instruction for permission");
        when(mailService.getSettingsUrl()).thenReturn("https://mail.com/settings");

        SendMessage result = gettingTokenStep.execute(update);

        assertEquals(Long.toString(chat.getId()), result.getChatId());
        assertEquals("Instruction for permission", result.getText());

        InlineKeyboardMarkup markup = (InlineKeyboardMarkup) result.getReplyMarkup();
        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();
        assertEquals(1, keyboard.size());
        assertEquals(2, keyboard.get(0).size());

        InlineKeyboardButton authButton = keyboard.get(0).get(0);
        assertEquals("Разрешить доступ к почтовому ящику", authButton.getText());
        assertEquals("https://mail.com/settings", authButton.getUrl());

        InlineKeyboardButton doneButton = keyboard.get(0).get(1);
        assertEquals("Выполнено ✅", doneButton.getText());
        assertEquals("/add_email " + RegistrationStep.PERMISSION_CONFIRMATION, doneButton.getCallbackData());
    }

    @Test
    void getRegistrationStep_shouldReturnGettingTokenStep() {
        RegistrationStep result = gettingTokenStep.getRegistrationStep();

        assertEquals(RegistrationStep.GETTING_TOKEN, result);
    }
}
