package ru.tbank.emailcheckerbot.bot.command.impl;

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
import ru.tbank.emailcheckerbot.entity.jpa.UserEmailJpaEntity;
import ru.tbank.emailcheckerbot.exeption.InvalidCallbackQueryException;
import ru.tbank.emailcheckerbot.service.user.UserEmailJpaService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveEmailCommandTest {

    @Mock
    private UserEmailJpaService userEmailJpaService;

    @InjectMocks
    private RemoveEmailCommand removeEmailCommand;

    @Test
    void execute_shouldHandleCallbackQueryAndRemoveEmail() {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(12345L);
        User user = new User();
        user.setId(67890L);
        message.setChat(chat);
        message.setFrom(user);
        callbackQuery.setMessage(message);
        callbackQuery.setData("/remove_email 1");
        update.setCallbackQuery(callbackQuery);

        when(userEmailJpaService.removeEmail(1L)).thenReturn("slavik@mail.com");

        SendMessage result = removeEmailCommand.execute(update);

        assertEquals(Long.toString(chat.getId()), result.getChatId());
        assertEquals("Данные о почтовом аккаунте slavik@mail.com были успешно удалены из бота", result.getText());
    }

    @Test
    void execute_shouldHandleCallbackQueryAndReturnEmailNotFoundMessage() {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(12345L);
        User user = new User();
        user.setId(67890L);
        message.setChat(chat);
        message.setFrom(user);
        callbackQuery.setMessage(message);
        callbackQuery.setData("/remove_email 1");
        update.setCallbackQuery(callbackQuery);

        when(userEmailJpaService.removeEmail(1L)).thenReturn(null);

        SendMessage result = removeEmailCommand.execute(update);

        assertEquals(Long.toString(chat.getId()), result.getChatId());
        assertEquals("Данные об этом почтовом аккаунте уже были удалены из бота", result.getText());
    }

    @Test
    void execute_shouldThrowExceptionWhenInvalidCallbackQuery() {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(12345L);
        User user = new User();
        user.setId(67890L);
        message.setChat(chat);
        message.setFrom(user);
        callbackQuery.setMessage(message);
        callbackQuery.setData("/remove_email");
        update.setCallbackQuery(callbackQuery);

        assertThrows(InvalidCallbackQueryException.class, () -> removeEmailCommand.execute(update));
    }

    @Test
    void execute_shouldHandleMessageAndReturnNoEmailsMessage() {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(12345L);
        User user = new User();
        user.setId(67890L);
        message.setChat(chat);
        message.setFrom(user);
        update.setMessage(message);

        when(userEmailJpaService.getEmailsList(67890L)).thenReturn(null);

        SendMessage result = removeEmailCommand.execute(update);

        assertEquals(Long.toString(chat.getId()), result.getChatId());
        assertEquals("Вы ещё не добавляли почтовых аккаунтов", result.getText());
    }

    @Test
    void execute_shouldHandleMessageAndReturnEmailsList() {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(12345L);
        User user = new User();
        user.setId(67890L);
        message.setChat(chat);
        message.setFrom(user);
        update.setMessage(message);

        UserEmailJpaEntity email1 = new UserEmailJpaEntity();
        email1.setId(1L);
        email1.setEmail("slavik@mail.com");
        UserEmailJpaEntity email2 = new UserEmailJpaEntity();
        email2.setId(2L);
        email2.setEmail("vyacheslav@mail.com");

        when(userEmailJpaService.getEmailsList(67890L)).thenReturn(List.of(email1, email2));

        SendMessage result = removeEmailCommand.execute(update);

        assertEquals(Long.toString(chat.getId()), result.getChatId());
        assertEquals(
                "Выберите почтовый аккаунт, данные о котором Вы бы хотели удалить из бота",
                result.getText()
        );

        InlineKeyboardMarkup markup = (InlineKeyboardMarkup) result.getReplyMarkup();
        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();
        assertEquals(2, keyboard.size());

        InlineKeyboardButton button1 = keyboard.get(0).get(0);
        assertEquals("slavik@mail.com", button1.getText());
        assertEquals("/remove_email 1", button1.getCallbackData());

        InlineKeyboardButton button2 = keyboard.get(1).get(0);
        assertEquals("vyacheslav@mail.com", button2.getText());
        assertEquals("/remove_email 2", button2.getCallbackData());
    }

    @Test
    void getName_shouldReturnRemoveEmailCommandName() {
        String result = removeEmailCommand.getName();

        assertEquals("/remove_email", result);
    }

    @Test
    void getDescription_shouldReturnRemoveEmailCommandDescription() {
        String result = removeEmailCommand.getDescription();

        assertEquals("команда для удаления данных о почтовом аккаунте из бота", result);
    }
}