package ru.tbank.emailcheckerbot.bot.command.registration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.tbank.emailcheckerbot.entity.MailProvider;
import ru.tbank.emailcheckerbot.service.UserEmailRedisService;

import java.util.ArrayList;
import java.util.List;

import static ru.tbank.emailcheckerbot.bot.util.TelegramUtils.createInlineKeyboardButton;

@Component
@RequiredArgsConstructor
public class InitialEmailRegistrationStep implements EmailRegistrationStep {

    private final UserEmailRedisService userEmailRedisService;

    @Override
    public SendMessage execute(Update update) {
        Message message = update.getMessage();
        Long userId = update.getMessage().getFrom().getId();

        userEmailRedisService.createUserEmailRedisEntity(userId, message.getChatId());

        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());
        response.setText("Пожалуйста, выберите почтового провайдера:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        for (MailProvider provider : MailProvider.values()) {
            buttons.add(
                    createInlineKeyboardButton(
                            provider.getTitle(),
                            "/add_email " + RegistrationStep.CHOOSING_PROVIDER + " " + provider,
                            null
                    )
            );
        }
        markup.setKeyboard(List.of(buttons));
        response.setReplyMarkup(markup);

        return response;
    }

    @Override
    public RegistrationStep getRegistrationStep() {
        return RegistrationStep.INITIAL;
    }
}