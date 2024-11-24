package ru.tbank.emailcheckerbot.bot.command.registration;

import lombok.RequiredArgsConstructor;
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

@Component
@RequiredArgsConstructor
public class ChoosingProviderStep implements EmailRegistrationStep {

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

        String providerName = update.getCallbackQuery().getData().split(" ")[2];
        MailProvider provider = MailProvider.valueOf(providerName);

        switch (provider) {

            case YANDEX -> {
                return handleProviderChoice(
                        chatId,
                        MailProvider.YANDEX.getTitle(),
                        yandexService.getAuthUrl(userId)
                );
            }

            case MAILRu -> {
                return handleProviderChoice(
                        chatId,
                        MailProvider.MAILRu.getTitle(),
                        mailRuService.getAuthUrl(userId)
                );
            }

            default -> throw new IllegalStateException("Unexpected value: " + provider);
        }
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

    private SendMessage getUserInactivityMessage(Long chatId) {
        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        String responseText = "Вы долго бездействовали, и запись о Вас была удалена.\n" +
                "Пожалуйста, начните процесс добавления почты с начала " + Command.ADD_EMAIL.getTitle();
        response.setText(responseText);

        return response;
    }
}
