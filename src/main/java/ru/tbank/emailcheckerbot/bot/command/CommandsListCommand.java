package ru.tbank.emailcheckerbot.bot.command;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommandsListCommand implements BotCommand {

    private final List<BotCommand> commandsList;

    @PostConstruct
    public void addMyselfIntoCommandsList() {
        commandsList.add(this);
    }

    @Override
    public SendMessage execute(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());

        StringBuilder text = new StringBuilder();
        for (BotCommand command : commandsList) {
            text.append(command.getName()).append(" - ").append(command.getDescription()).append("\n");
        }
        sendMessage.setText(text.toString());

        return sendMessage;
    }

    @Override
    public String getName() {
        return Command.COMMANDS.getTitle();
    }

    @Override
    public String getDescription() {
        return "команда для вывода списка команд";
    }
}
