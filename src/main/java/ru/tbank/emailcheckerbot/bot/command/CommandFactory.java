package ru.tbank.emailcheckerbot.bot.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommandFactory {

    private final Map<String, BotCommand> commands = new HashMap<>();

    @Autowired
    public CommandFactory(List<BotCommand> commandsList) {
        for (BotCommand command : commandsList) {
            commands.put(command.getName(), command);
        }
    }

    public BotCommand getCommand(String command) {
        return commands.getOrDefault(command, null);
    }
}
