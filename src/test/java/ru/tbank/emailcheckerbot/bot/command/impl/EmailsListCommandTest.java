package ru.tbank.emailcheckerbot.bot.command.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.tbank.emailcheckerbot.entity.jpa.UserEmailJpaEntity;
import ru.tbank.emailcheckerbot.service.user.UserEmailJpaService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailsListCommandTest {

    @Mock
    private UserEmailJpaService userEmailJpaService;

    @InjectMocks
    private EmailsListCommand emailsListCommand;

    @Test
    void execute_shouldReturnMessageWithNoEmails() {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(12345L);
        User user = new User();
        user.setId(67890L);
        message.setChat(chat);
        message.setFrom(user);
        update.setMessage(message);

        when(userEmailJpaService.getEmailsList(67890L)).thenReturn(null);

        SendMessage result = emailsListCommand.execute(update);

        assertEquals(Long.toString(chat.getId()), result.getChatId());
        assertEquals("Вы ещё не добавляли почтовых аккаунтов", result.getText());
    }

    @Test
    void execute_shouldReturnMessageWithEmailsList() {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(12345L);
        User user = new User();
        user.setId(67890L);
        message.setChat(chat);
        message.setFrom(user);
        update.setMessage(message);

        UserEmailJpaEntity email1 = new UserEmailJpaEntity();
        email1.setEmail("slavik@mail.com");
        UserEmailJpaEntity email2 = new UserEmailJpaEntity();
        email2.setEmail("vyacheslav@mail.ru");

        when(userEmailJpaService.getEmailsList(67890L)).thenReturn(List.of(email1, email2));

        SendMessage result = emailsListCommand.execute(update);

        assertEquals(Long.toString(chat.getId()), result.getChatId());
        assertEquals(
                "Список добавленных почтовых аккаунтов:\nslavik@mail.com\nvyacheslav@mail.ru",
                result.getText()
        );
    }

    @Test
    void getName_shouldReturnEmailsListCommandName() {
        String result = emailsListCommand.getName();

        assertEquals("/emails", result);
    }

    @Test
    void getDescription_shouldReturnEmailsListCommandDescription() {
        String result = emailsListCommand.getDescription();

        assertEquals("команда для просмотра списка добавленных почтовых аккаунтов", result);
    }
}
