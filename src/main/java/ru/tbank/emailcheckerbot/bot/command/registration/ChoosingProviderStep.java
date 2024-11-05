package ru.tbank.emailcheckerbot.bot.command.registration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.tbank.emailcheckerbot.service.UserStateService;
import ru.tbank.emailcheckerbot.service.YandexService;

import java.util.List;

import static ru.tbank.emailcheckerbot.bot.util.TelegramUtils.createInlineKeyboardButton;

@Component
@RequiredArgsConstructor
public class ChoosingProviderStep implements EmailRegistrationStep {

    private final YandexService yandexService;
    private final UserStateService userStateService;


    @Override
    public SendMessage execute(Update update) {
        String providerName = update.getCallbackQuery().getData().split(" ")[2];
        MailProvider provider = MailProvider.valueOf(providerName);

        SendMessage response;

        switch (provider) {

            case YANDEX -> response = handleYandexProviderChoice(update);

            case GOOGLE -> response = handleGoogleProviderChoice(update);

            case MAILRu -> response = handleMailRuProviderChoice(update);

            default -> throw new IllegalStateException("Unexpected value: " + provider);
        }

        return response;
    }

    private SendMessage handleYandexProviderChoice(Update update) {
        Long userId = update.getCallbackQuery().getFrom().getId();
        userStateService.setEmailProvider(userId, MailProvider.YANDEX.getConfigurationName());
        userStateService.setStep(userId, RegistrationStep.WAITING_FOR_TOKEN);
        String authUrl = yandexService.getAuthUrl();
        String responseText = "Для добавления Yandex почты, пожалуйста, авторизуйтесь по ссылке ниже и отправьте полученный токен в этот чат.";

        SendMessage response = new SendMessage();
        response.setChatId(update.getCallbackQuery().getMessage().getChatId());
        response.setText(responseText);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> buttons = List.of(
                createInlineKeyboardButton(
                        "Авторизоваться",
                        "/add_email authorization_done",
                        authUrl)
        );
        markup.setKeyboard(List.of(buttons));
        response.setReplyMarkup(markup);

        return response;
    }

    private SendMessage handleGoogleProviderChoice(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        sendMessage.setText("Извините, поддержка этого провайдера пока не реализована.");

        return sendMessage;
    }

    private SendMessage handleMailRuProviderChoice(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        sendMessage.setText("Извините, поддержка этого провайдера пока не реализована.");

        return sendMessage;
    }
}
