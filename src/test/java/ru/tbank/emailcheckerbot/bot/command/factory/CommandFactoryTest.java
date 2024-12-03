package ru.tbank.emailcheckerbot.bot.command.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.emailcheckerbot.bot.command.BotCommand;
import ru.tbank.emailcheckerbot.bot.command.impl.AddEmailCommand;
import ru.tbank.emailcheckerbot.bot.command.impl.CommandsListCommand;
import ru.tbank.emailcheckerbot.bot.command.impl.StartCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandFactoryTest {

    @Mock
    private StartCommand startCommand;

    @Mock
    private AddEmailCommand addEmailCommand;

    @Mock
    private CommandsListCommand commandsListCommand;

    @InjectMocks
    private CommandFactory commandFactory;

    @Spy
    private List<BotCommand> list = new ArrayList<>();

    @BeforeEach
    public void setup() {
        list.add(startCommand);
        list.add(addEmailCommand);
        list.add(commandsListCommand);
    }

    @Test
    void getCommand_shouldReturnCorrectCommand() {
        when(startCommand.getName()).thenReturn("/start");
        when(addEmailCommand.getName()).thenReturn("/add_email");
        when(commandsListCommand.getName()).thenReturn("/commands");
        commandFactory = new CommandFactory(list);

        BotCommand result = commandFactory.getCommand("/start");

        assertEquals(startCommand, result);
    }

    @Test
    void getCommand_shouldThrowExceptionWhenCommandNotFound() {
        when(startCommand.getName()).thenReturn("/start");
        when(addEmailCommand.getName()).thenReturn("/add_email");
        when(commandsListCommand.getName()).thenReturn("/commands");
        commandFactory = new CommandFactory(list);

        assertThrows(NoSuchElementException.class, () -> commandFactory.getCommand("/unknown"));
    }
}
