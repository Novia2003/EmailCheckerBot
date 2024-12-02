package ru.tbank.emailcheckerbot.bot.util;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static org.junit.jupiter.api.Assertions.*;

class TelegramUtilsTest {

    @Test
    void createInlineKeyboardButton_shouldCreateButtonWithCallbackData() {
        String text = "Нажми меня";
        String callbackData = "callback";

        InlineKeyboardButton button = TelegramUtils.createInlineKeyboardButton(text, callbackData, null);

        assertEquals(text, button.getText());
        assertEquals(callbackData, button.getCallbackData());
        assertNull(button.getUrl());
    }

    @Test
    void createInlineKeyboardButton_shouldCreateButtonWithUrl() {
        String text = "Нажми меня";
        String url = "http://mail.com";

        InlineKeyboardButton button = TelegramUtils.createInlineKeyboardButton(text, null, url);

        assertEquals(text, button.getText());
        assertNull(button.getCallbackData());
        assertEquals(url, button.getUrl());
    }

    @Test
    void createInlineKeyboardButton_shouldCreateButtonWithTextOnly() {
        String text = "Нажми меня";

        InlineKeyboardButton button = TelegramUtils.createInlineKeyboardButton(text, null, null);

        assertEquals(text, button.getText());
        assertNull(button.getCallbackData());
        assertNull(button.getUrl());
    }

    @Test
    void getUserInactivityMessage_shouldReturnCorrectMessage() {
        long chatId = 12345L;
        String expectedText = "Вы долго бездействовали, и запись о Вас была удалена.\n" +
                "Пожалуйста, начните процесс добавления почты с начала /add_email";

        SendMessage message = TelegramUtils.getUserInactivityMessage(chatId);

        assertEquals(Long.toString(chatId), message.getChatId());
        assertEquals(expectedText, message.getText());
    }
}
