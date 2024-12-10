package ru.tbank.emailcheckerbot.bot.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.emailcheckerbot.bot.command.BotCommand;
import ru.tbank.emailcheckerbot.bot.command.Command;
import ru.tbank.emailcheckerbot.entity.jpa.UserEmailJpaEntity;
import ru.tbank.emailcheckerbot.service.user.UserEmailJpaService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EmailsListCommand implements BotCommand {

    private final UserEmailJpaService userEmailJpaService;

    @Override
    public SendMessage execute(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        List<UserEmailJpaEntity> emails = userEmailJpaService.getEmailsList(userId);

        StringBuilder builder = new StringBuilder();
        if (emails == null || emails.isEmpty()) {
            builder.append("Вы ещё не добавляли почтовых аккаунтов");
        } else {
            builder.append("Список добавленных почтовых аккаунтов:");

            for (UserEmailJpaEntity email : emails) {
                builder.append("\n").append(email.getEmail());
            }
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(builder.toString());

        return sendMessage;
    }

    @Override
    public String getName() {
        return Command.EMAILS_LIST.getTitle();
    }

    @Override
    public String getDescription() {
        return "команда для просмотра списка добавленных почтовых аккаунтов";
    }
}
