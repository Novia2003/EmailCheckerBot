package ru.tbank.emailcheckerbot.bot.command.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.emailcheckerbot.bot.command.BotCommand;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandsListCommandTest {

    @Mock
    private StartCommand startCommand;
    
    @Mock
    private AddEmailCommand addEmailCommand;

    @InjectMocks
    private CommandsListCommand commandsListCommand;

    @Spy
    private List<BotCommand> list = new ArrayList<>();

    @BeforeEach
    public void setup() {
        list.add(startCommand);
        list.add(addEmailCommand);
    }

    @Test
    void execute_shouldReturnListOfCommands() {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(12345L);
        message.setChat(chat);
        update.setMessage(message);
        commandsListCommand.addMyselfIntoCommandsList();

        when(startCommand.getName()).thenReturn("/start");
        when(startCommand.getDescription()).thenReturn("команда с описанием предназначения бота");
        when(addEmailCommand.getName()).thenReturn("/add_email");
        when(addEmailCommand.getDescription()).thenReturn("команда для добавления нового почтового аккаунта");

        SendMessage result = commandsListCommand.execute(update);

        assertEquals(Long.toString(12345L), result.getChatId());
        String expectedText = """
                /start - команда с описанием предназначения бота
                /add_email - команда для добавления нового почтового аккаунта
                /commands - команда для вывода списка команд
                """;
        assertEquals(expectedText.trim(), result.getText().trim());
    }

    @Test
    void getName_shouldReturnCommandsCommandName() {
        String result = commandsListCommand.getName();

        assertEquals("/commands", result);
    }

    @Test
    void getDescription_shouldReturnCommandsCommandDescription() {
        String result = commandsListCommand.getDescription();

        assertEquals("команда для вывода списка команд", result);
    }

    @Test
    void addMyselfIntoCommandsList_shouldAddCommandToList() {
        commandsListCommand.addMyselfIntoCommandsList();

        verify(list).add(commandsListCommand);
    }
}
