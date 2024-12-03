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
import ru.tbank.emailcheckerbot.exeption.InvalidCallbackQueryException;
import ru.tbank.emailcheckerbot.service.user.UserEmailRedisService;
import ru.tbank.emailcheckerbot.service.provider.MailService;
import ru.tbank.emailcheckerbot.service.provider.factory.MailServiceFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.tbank.emailcheckerbot.bot.util.TelegramUtils.getUserInactivityMessage;

@ExtendWith(MockitoExtension.class)
class ChoosingProviderStepTest {

    @Mock
    private MailServiceFactory mailServiceFactory;

    @Mock
    private UserEmailRedisService userEmailRedisService;

    @Mock
    private MailService mailService;

    @InjectMocks
    private ChoosingProviderStep choosingProviderStep;

    @Test
    void execute_shouldReturnUserInactivityMessageWhenUserEmailRecordNotExists() {
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

        when(userEmailRedisService.isUserEmailRecordNotExists(user.getId())).thenReturn(true);

        SendMessage result = choosingProviderStep.execute(update);

        assertEquals(getUserInactivityMessage(chat.getId()), result);
    }

    @Test
    void execute_shouldThrowExceptionWhenInvalidCallbackQuery() {
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
        callbackQuery.setData("add_email CHOOSING_PROVIDER");
        update.setCallbackQuery(callbackQuery);

        when(userEmailRedisService.isUserEmailRecordNotExists(user.getId())).thenReturn(false);

        assertThrows(InvalidCallbackQueryException.class, () -> choosingProviderStep.execute(update));
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
        callbackQuery.setData("add_email CHOOSING_PROVIDER YANDEX");
        update.setCallbackQuery(callbackQuery);

        when(userEmailRedisService.isUserEmailRecordNotExists(user.getId())).thenReturn(false);
        when(mailServiceFactory.getService(MailProvider.YANDEX)).thenReturn(mailService);
        when(mailService.getAuthUrl(user.getId())).thenReturn(
                "https://oauth.yandex.ru/authorize?response_type=code&client_id=e0261684b7&state=12345%20YANDEX"
        );

        SendMessage result = choosingProviderStep.execute(update);

        assertEquals(Long.toString(chat.getId()), result.getChatId());
        assertEquals("Для добавления Yandex почты, пожалуйста, авторизуйтесь по ссылке ниже", result.getText());

        InlineKeyboardMarkup markup = (InlineKeyboardMarkup) result.getReplyMarkup();
        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();
        assertEquals(1, keyboard.size());
        assertEquals(2, keyboard.get(0).size());

        InlineKeyboardButton authButton = keyboard.get(0).get(0);
        assertEquals("Авторизоваться", authButton.getText());
        assertEquals(
                "https://oauth.yandex.ru/authorize?response_type=code&client_id=e0261684b7&state=12345%20YANDEX",
                authButton.getUrl()
        );

        InlineKeyboardButton doneButton = keyboard.get(0).get(1);
        assertEquals("Выполнено ✅", doneButton.getText());
        assertEquals("/add_email " + RegistrationStep.GETTING_TOKEN, doneButton.getCallbackData());
    }

    @Test
    void getRegistrationStep_shouldReturnChoosingProviderStep() {
        RegistrationStep result = choosingProviderStep.getRegistrationStep();

        assertEquals(RegistrationStep.CHOOSING_PROVIDER, result);
    }
}
