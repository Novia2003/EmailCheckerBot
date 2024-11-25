package ru.tbank.emailcheckerbot.bot.command.registration.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.tbank.emailcheckerbot.bot.command.registration.EmailRegistrationStep;
import ru.tbank.emailcheckerbot.bot.command.registration.RegistrationStep;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.exeption.InvalidCallbackQueryException;
import ru.tbank.emailcheckerbot.service.user.UserEmailRedisService;
import ru.tbank.emailcheckerbot.service.provider.factory.MailServiceFactory;

import java.util.List;

import static ru.tbank.emailcheckerbot.bot.util.TelegramUtils.createInlineKeyboardButton;
import static ru.tbank.emailcheckerbot.bot.util.TelegramUtils.getUserInactivityMessage;

@Component
@RequiredArgsConstructor
public class ChoosingProviderStep implements EmailRegistrationStep {

    private final MailServiceFactory mailServiceFactory;

    private final UserEmailRedisService userEmailRedisService;


    @Override
    public SendMessage execute(Update update) {
        Long userId = update.getCallbackQuery().getFrom().getId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (userEmailRedisService.isUserEmailRecordNotExists(userId)) {
            return getUserInactivityMessage(chatId);
        }

        String[] callbackQueryData = update.getCallbackQuery().getData().split(" ");

        if (callbackQueryData.length < 3) {
            throw new InvalidCallbackQueryException("Incorrect number of words in the callbackQuery");
        }

        String providerName = callbackQueryData[2];
        MailProvider provider = MailProvider.valueOf(providerName);

        return handleProviderChoice(
                chatId,
                provider.getTitle(),
                mailServiceFactory.getService(provider).getAuthUrl(userId)
        );
    }

    private SendMessage handleProviderChoice(Long chatId, String providerTitle, String authUrl) {
        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        String responseText = "Для добавления " + providerTitle +
                " почты, пожалуйста, авторизуйтесь по ссылке ниже";
        response.setText(responseText);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> buttons = List.of(
                createInlineKeyboardButton(
                        "Авторизоваться",
                        null,
                        authUrl
                ),
                createInlineKeyboardButton(
                        "Выполнено ✅",
                        "/add_email " + RegistrationStep.GETTING_TOKEN,
                        null
                )
        );
        markup.setKeyboard(List.of(buttons));
        response.setReplyMarkup(markup);

        return response;
    }

    @Override
    public RegistrationStep getRegistrationStep() {
        return RegistrationStep.CHOOSING_PROVIDER;
    }
}
