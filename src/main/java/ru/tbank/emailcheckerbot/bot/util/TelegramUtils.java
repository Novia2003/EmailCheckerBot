package ru.tbank.emailcheckerbot.bot.util;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.tbank.emailcheckerbot.bot.command.Command;

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

    public static SendMessage getUserInactivityMessage(Long chatId) {
        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        String responseText = "Вы долго бездействовали, и запись о Вас была удалена.\n" +
                "Пожалуйста, начните процесс добавления почты с начала " + Command.ADD_EMAIL.getTitle();
        response.setText(responseText);

        return response;
    }
}
