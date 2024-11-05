package ru.tbank.emailcheckerbot.bot.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class TelegramUtils {

    public static InlineKeyboardButton createInlineKeyboardButton(String text, String callbackData, String url) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);

        if (callbackData != null) {
            button.setCallbackData(callbackData);
        }

        if (url != null) {
            button.setUrl(url);
        }

        return button;
    }
}
