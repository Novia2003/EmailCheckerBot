package ru.tbank.emailcheckerbot.bot.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.tbank.emailcheckerbot.bot.command.BotCommand;
import ru.tbank.emailcheckerbot.bot.command.Command;
import ru.tbank.emailcheckerbot.entity.jpa.UserEmailJpaEntity;
import ru.tbank.emailcheckerbot.exeption.InvalidCallbackQueryException;
import ru.tbank.emailcheckerbot.service.user.UserEmailJpaService;

import java.util.ArrayList;
import java.util.List;

import static ru.tbank.emailcheckerbot.bot.util.TelegramUtils.createInlineKeyboardButton;

@Component
@RequiredArgsConstructor
public class RemoveEmailCommand implements BotCommand {

    private final UserEmailJpaService userEmailJpaService;

    @Override
    public SendMessage execute(Update update) {
        return (update.hasCallbackQuery()) ? handleCallbackQuery(update) : handleMessage(update);
    }

    private SendMessage handleCallbackQuery(Update update) {
        String[] callbackQueryData = update.getCallbackQuery().getData().split(" ");

        if (callbackQueryData.length < 2) {
            throw new InvalidCallbackQueryException("Incorrect number of words in the callbackQuery");
        }

        Long userEmailId = Long.parseLong(callbackQueryData[1]);

        String email = userEmailJpaService.removeEmail(userEmailId);

        SendMessage response = new SendMessage();
        response.setChatId(update.getCallbackQuery().getMessage().getChatId());

        if (email != null) {
            response.setText("Данные о почтовом аккаунте " + email + " были успешно удалены из бота");
        } else {
            response.setText("Данные об этом почтовом аккаунте уже были удалены из бота");
        }

        return response;
    }

    private SendMessage handleMessage(Update update) {
        SendMessage response = new SendMessage();
        response.setChatId(update.getMessage().getChatId());

        Long userId = update.getMessage().getFrom().getId();
        List<UserEmailJpaEntity> emails = userEmailJpaService.getEmailsList(userId);

        if (emails == null || emails.isEmpty()) {
            response.setText("Вы ещё не добавляли почтовых аккаунтов");
            return response;
        }

        response.setText("Выберите почтовый аккаунт, данные о котором Вы бы хотели удалить из бота");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> button = new ArrayList<>();
        for (UserEmailJpaEntity email : emails) {
            button.add(List.of(
                    createInlineKeyboardButton(
                            email.getEmail(),
                            Command.REMOVE_EMAIL.getTitle() + " " + email.getId(),
                            null
                    )
            ));
        }
        markup.setKeyboard(button);
        response.setReplyMarkup(markup);

        return response;
    }

    @Override
    public String getName() {
        return Command.REMOVE_EMAIL.getTitle();
    }

    @Override
    public String getDescription() {
        return "команда для удаления данных о почтовом аккаунте из бота";
    }
}