package ru.tbank.emailcheckerbot.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.tbank.emailcheckerbot.bot.TelegramBot;
import ru.tbank.emailcheckerbot.configuration.property.MessagesProperties;
import ru.tbank.emailcheckerbot.dto.message.EmailMessageDTO;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.entity.jpa.UserEmailJpaEntity;
import ru.tbank.emailcheckerbot.entity.jpa.UserJpaEntity;
import ru.tbank.emailcheckerbot.service.encryption.EncryptionService;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private MessagesProperties messagesProperties;

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void notifyUser_shouldSendNotificationForEachNewMessage() {
        EmailMessageDTO[] newMessages = {
                createEmailMessageDTO("Всё будет хорошо"),
                createEmailMessageDTO("Всё будет хорошо")
        };
        UserEmailJpaEntity userEmailJpaEntity = createUserEmailJpaEntity();

        when(messagesProperties.getUrl()).thenReturn("http://127.0.0.1:8080/api/v1/messages");

        notificationService.notifyUser(newMessages, userEmailJpaEntity);

        verify(telegramBot, times(2)).executeMessage(any(SendMessage.class));
    }

    @Test
    void notifyUser_shouldFormatNotificationMessageCorrectly() {
        EmailMessageDTO message = createEmailMessageDTO("Всё будет хорошо");
        UserEmailJpaEntity userEmailJpaEntity = createUserEmailJpaEntity();

        when(messagesProperties.getUrl()).thenReturn("http://127.0.0.1:8080/api/v1/messages");

        notificationService.notifyUser(new EmailMessageDTO[]{message}, userEmailJpaEntity);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).executeMessage(captor.capture());

        SendMessage sendMessage = captor.getValue();
        String expected = """
                *📬 Новое сообщение на почте:* slavik@mail.com
                
                *Отправитель:* irishkakislova@mail.ru
                
                *Тема:* <Без темы>
                
                *Сообщение:* Всё будет хорошо
                """;
        assertEquals(expected.trim(), sendMessage.getText().trim());
    }

    @Test
    void notifyUser_shouldExtractSnippetFromMessageCorrectly() {
        EmailMessageDTO message = createEmailMessageDTO("Short content");
        UserEmailJpaEntity userEmailJpaEntity = createUserEmailJpaEntity();

        when(messagesProperties.getUrl()).thenReturn("http://127.0.0.1:8080/api/v1/messages");

        notificationService.notifyUser(new EmailMessageDTO[]{message}, userEmailJpaEntity);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).executeMessage(captor.capture());

        SendMessage sendMessage = captor.getValue();
        assertTrue(sendMessage.getText().contains("Short content"));
    }

    @Test
    void notifyUser_shouldTruncateLongSnippetFromMessageCorrectly() {
        EmailMessageDTO message = createEmailMessageDTO(
                "This is a very long content that should be truncated because " +
                        "it exceeds the maximum snippet length!!!"
        );
        UserEmailJpaEntity userEmailJpaEntity = createUserEmailJpaEntity();

        when(messagesProperties.getUrl()).thenReturn("http://127.0.0.1:8080/api/v1/messages");

        notificationService.notifyUser(new EmailMessageDTO[]{message}, userEmailJpaEntity);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).executeMessage(captor.capture());

        SendMessage sendMessage = captor.getValue();
        assertTrue(sendMessage.getText().contains(
                "This is a very long content that should be truncated because it exceeds the maximum snippet length!!..."
        ));
    }

    @Test
    void notifyUser_shouldGenerateCorrectMessageLink() {
        EmailMessageDTO message = createEmailMessageDTO("Всё будет хорошо");
        UserEmailJpaEntity userEmailJpaEntity = createUserEmailJpaEntity();

        when(messagesProperties.getUrl()).thenReturn("http://127.0.0.1:8080/api/v1/messages");
        when(encryptionService.encodeId(anyLong())).thenReturn("encodedId");

        notificationService.notifyUser(new EmailMessageDTO[]{message}, userEmailJpaEntity);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).executeMessage(captor.capture());

        SendMessage sendMessage = captor.getValue();
        InlineKeyboardMarkup markup = (InlineKeyboardMarkup) sendMessage.getReplyMarkup();
        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();
        InlineKeyboardButton button = keyboard.get(0).get(0);
        assertEquals(
                "http://127.0.0.1:8080/api/v1/messages?encodedUserEmailId=encodedId&encodedMessageUID=encodedId",
                button.getUrl()
        );
    }


    private EmailMessageDTO createEmailMessageDTO(String content) {
        return new EmailMessageDTO(
                1L,
                "irishkakislova@mail.ru",
                "<Без темы>",
                content
        );
    }

    private UserEmailJpaEntity createUserEmailJpaEntity() {
        UserJpaEntity user = new UserJpaEntity();
        user.setId(1L);
        user.setTelegramId(1L);
        user.setChatId(1L);

        UserEmailJpaEntity entity = new UserEmailJpaEntity();
        entity.setId(1L);
        entity.setEmail("slavik@mail.com");
        entity.setAccessToken("accessToken");
        entity.setMailProvider(MailProvider.YANDEX);
        entity.setEndAccessTokenLife(Instant.now().plusSeconds(3600));
        entity.setLastMessageUID(100L);
        entity.setUser(user);
        return entity;
    }
}
