package ru.tbank.emailcheckerbot.bot.command.registration.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.tbank.emailcheckerbot.bot.command.registration.EmailRegistrationStep;
import ru.tbank.emailcheckerbot.bot.command.registration.RegistrationStep;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.exeption.UserEmailRedisEntityNotFoundException;
import ru.tbank.emailcheckerbot.service.user.UserEmailRedisService;
import ru.tbank.emailcheckerbot.service.provider.MailService;
import ru.tbank.emailcheckerbot.service.provider.factory.MailServiceFactory;

import java.util.List;

import static ru.tbank.emailcheckerbot.bot.util.TelegramUtils.createInlineKeyboardButton;
import static ru.tbank.emailcheckerbot.bot.util.TelegramUtils.getUserInactivityMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class GettingTokenStep implements EmailRegistrationStep {

    private final MailServiceFactory mailServiceFactory;

    private final UserEmailRedisService userEmailRedisService;

    @Override
    public SendMessage execute(Update update) {
        Long userId = update.getCallbackQuery().getFrom().getId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        try {
            if (userEmailRedisService.getAccessToken(userId) == null) {
                return getFailedOauthMessage(chatId);
            }

            MailProvider mailProvider = userEmailRedisService.getMailProvider(userId);
            MailService mailService = mailServiceFactory.getService(mailProvider);

            return handleProviderChoice(
                    chatId,
                    mailService.getInstructionForPermission(),
                    mailService.getSettingsUrl()
            );
        } catch (UserEmailRedisEntityNotFoundException e) {
            return getUserInactivityMessage(chatId);
        }
    }

    private SendMessage handleProviderChoice(Long chatId, String responseText, String settingsUrl) {
        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        response.setText(responseText);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> buttons = List.of(
                createInlineKeyboardButton(
                        "Разрешить доступ к почтовому ящику",
                        null,
                        settingsUrl
                ),
                createInlineKeyboardButton(
                        "Выполнено ✅",
                        "/add_email " + RegistrationStep.PERMISSION_CONFIRMATION,
                        null
                )
        );
        markup.setKeyboard(List.of(buttons));
        response.setReplyMarkup(markup);

        return response;
    }

    @Override
    public RegistrationStep getRegistrationStep() {
        return RegistrationStep.GETTING_TOKEN;
    }

    private SendMessage getFailedOauthMessage(Long chatId) {
        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        String responseText = "Токен доступа не был получен.\n" +
                "Пожалуйста, повторите попытку авторизации";
        response.setText(responseText);

        return response;
    }
}
