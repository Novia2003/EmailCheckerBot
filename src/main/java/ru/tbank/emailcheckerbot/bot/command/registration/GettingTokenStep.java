package ru.tbank.emailcheckerbot.bot.command.registration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.tbank.emailcheckerbot.service.UserStateService;
import ru.tbank.emailcheckerbot.service.YandexService;

import java.util.List;

import static ru.tbank.emailcheckerbot.bot.util.TelegramUtils.createInlineKeyboardButton;

@Slf4j
@Component
@RequiredArgsConstructor
public class GettingTokenStep implements EmailRegistrationStep {

    private final YandexService yandexService;
    private final UserStateService userStateService;

    @Override
    public SendMessage execute(Update update) {
        Message message = update.getMessage();

        String token = message.getText();
        Long userId = message.getFrom().getId();

        userStateService.setEmail(userId, yandexService.getEmail(token));
        userStateService.setToken(userId, token);
        userStateService.setStep(message.getFrom().getId(), RegistrationStep.WAITING_FOR_PERMISSION_CONFIRMATION);

        String responseText = "Теперь необходимо перейти по ссылке ниже и" +
                " разрешить доступ к почтовому ящику с помощью почтовых клиентов.";

        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());
        response.setText(responseText);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> buttons = List.of(
                createInlineKeyboardButton("Перейти к настройкам", null, yandexService.getSettingsUrl()),
                createInlineKeyboardButton("Выполнено ✅", "/add_email confirmation_done", null)
        );
        markup.setKeyboard(List.of(buttons));
        response.setReplyMarkup(markup);

        return response;
    }
}
