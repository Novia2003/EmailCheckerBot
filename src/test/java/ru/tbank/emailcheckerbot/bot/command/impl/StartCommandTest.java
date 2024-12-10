package ru.tbank.emailcheckerbot.bot.command.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StartCommandTest {

    @InjectMocks
    private StartCommand startCommand;

    @Test
    void execute_shouldReturnWelcomeMessage() {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(12345L);
        message.setChat(chat);
        update.setMessage(message);

        SendMessage result = startCommand.execute(update);

        assertEquals(Long.toString(12345L), result.getChatId());
        assertEquals("Добро пожаловать в EmailCheckerBot! Этот бот позволяет проверять новые сообщения на нескольких почтовых аккаунтах сразу.", result.getText());
    }

    @Test
    void getName_shouldReturnStartCommandName() {
        String result = startCommand.getName();

        assertEquals("/start", result);
    }

    @Test
    void getDescription_shouldReturnStartCommandDescription() {
        String result = startCommand.getDescription();

        assertEquals("команда с описанием предназначения бота", result);
    }
}
