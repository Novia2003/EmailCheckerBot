package ru.tbank.emailcheckerbot.bot.command.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.tbank.emailcheckerbot.bot.command.BotCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
        BotCommand botCommand = commands.get(command);

        if (botCommand == null) {
            throw new NoSuchElementException("No found action for command: " + command);
        }

        return botCommand;
    }
}
