package ru.tbank.emailcheckerbot.bot.command.registration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.tbank.emailcheckerbot.bot.command.Command;
import ru.tbank.emailcheckerbot.entity.MailProvider;
import ru.tbank.emailcheckerbot.service.MailRuService;
import ru.tbank.emailcheckerbot.service.UserEmailRedisService;
import ru.tbank.emailcheckerbot.service.YandexService;

import java.util.List;

import static ru.tbank.emailcheckerbot.bot.util.TelegramUtils.createInlineKeyboardButton;

@Slf4j
@Component
@RequiredArgsConstructor
public class GettingTokenStep implements EmailRegistrationStep {

    private final YandexService yandexService;
    private final MailRuService mailRuService;
    private final UserEmailRedisService userEmailRedisService;

    @Override
    public SendMessage execute(Update update) {
        Long userId = update.getCallbackQuery().getFrom().getId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (userEmailRedisService.isUserEmailRecordNotExists(userId)) {
            return getUserInactivityMessage(chatId);
        }

        if (userEmailRedisService.getAccessToken(userId) == null) {
            return getFailedOauthMessage(chatId);
        }

        MailProvider mailProvider = userEmailRedisService.getMailProvider(userId);

        switch (mailProvider) {
            case YANDEX -> {
                return handleProviderChoice(chatId, getResponseTextForYandex(), yandexService.getSettingsUrl());
            }

            case MAILRu -> {
                String responseText = "Теперь необходимо перейти по ссылке ниже и разрешить доступ к почтовому ящику " +
                        "с помощью почтовых клиентов, следуя описанной там инструкции";

                return handleProviderChoice(chatId, responseText, mailRuService.getSettingsUrl());
            }

            default -> throw new IllegalStateException("Unexpected value: " + mailProvider);
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

    private SendMessage getUserInactivityMessage(Long chatId) {
        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        String responseText = "Вы долго бездействовали, и запись о Вас была удалена.\n" +
                "Пожалуйста, начните процесс добавления почты с начала " + Command.ADD_EMAIL.getTitle();
        response.setText(responseText);

        return response;
    }

    private SendMessage getFailedOauthMessage(Long chatId) {
        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        String responseText = "Токен доступа не был получен.\n" +
                "Пожалуйста, повторите попытку авторизации";
        response.setText(responseText);

        return response;
    }

    private String getResponseTextForYandex() {
        return """
                Теперь необходимо перейти по ссылке ниже и разрешить доступ к почтовому ящику с помощью почтовых клиентов.
                Если Вы проходите авторизацию используя мобильный телефон:
                1. После перехода по ссылке откройте боковое меню в верхнем левом углу.
                2. Мотните вниз экрана и нажмите на кнопку "Полная версия".
                3. Нажмите на шестеренку в крайнем правом углу и нажмите на кнопку "Все настройки".
                4. В левой части экрана появится список, где необходимо выбрать "Почтовые программы".
                5. Теперь Вы можете разрешить доступ к почтовому ящику с помощью почтовых клиентов
                """;
    }
}
