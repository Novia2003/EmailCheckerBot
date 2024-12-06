package ru.tbank.emailcheckerbot.service.notification;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.tbank.emailcheckerbot.bot.TelegramBot;
import ru.tbank.emailcheckerbot.configuration.property.MessagesProperties;
import ru.tbank.emailcheckerbot.entity.jpa.UserEmailJpaEntity;
import ru.tbank.emailcheckerbot.dto.message.EmailMessageDTO;
import ru.tbank.emailcheckerbot.exeption.EncryptionException;
import ru.tbank.emailcheckerbot.service.encryption.EncryptionService;

import javax.mail.MessagingException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static ru.tbank.emailcheckerbot.bot.util.TelegramUtils.createInlineKeyboardButton;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Integer MAX_SNIPPET_LENGTH = 100;

    private final MessagesProperties messagesProperties;

    private final TelegramBot telegramBot;

    private final EncryptionService encryptionService;

    public void notifyUser(EmailMessageDTO[] newMessages, UserEmailJpaEntity userEmailJpaEntity) {
        Arrays.stream(newMessages).forEach(message -> {
            try {
                String notificationText = formatNotificationMessage(
                        message,
                        userEmailJpaEntity.getEmail()
                );

                String messageLink = getMessageLink(message.getUid(), userEmailJpaEntity.getId());
                sendNotification(userEmailJpaEntity.getUser().getChatId(), notificationText, messageLink);
            } catch (RuntimeException | MessagingException e) {
                log.error("Ошибка при формировании уведомления: {}", e.getMessage());
            }
        });
    }

    private String formatNotificationMessage(EmailMessageDTO message, String userEmail) throws MessagingException {
        String senderEmail = message.getFrom();
        String subject = message.getSubject();
        String snippet = extractSnippetFromMessage(message);

        return String.format(
                """
                        *📬 Новое сообщение на почте:* %s
                        
                        *Отправитель:* %s
                        
                        *Тема:* %s
                        
                        *Сообщение:* %s
                        """,
                userEmail, senderEmail, Optional.ofNullable(subject).orElse("Без темы"), snippet
        );
    }


    private String extractSnippetFromMessage(EmailMessageDTO message) {
        String content = message.getContent();

        String text = Jsoup.parse(content).text();

        if (text.length() <= MAX_SNIPPET_LENGTH) {
            FlexmarkHtmlConverter converter = FlexmarkHtmlConverter.builder().build();
            return converter.convert(content);
        } else {
            return text.substring(0, MAX_SNIPPET_LENGTH) + "...";
        }
    }

    private String getMessageLink(Long uid, Long userEmailEntityId) {
        try {
            String userEmailId = "encodedUserEmailId=" + encryptionService.encodeId(userEmailEntityId);
            String messageUID = "encodedMessageUID=" + encryptionService.encodeId(uid);

            return messagesProperties.getUrl() + "?" + userEmailId + "&" + messageUID;
        } catch (EncryptionException e) {
            log.error("Ошибка при формировании ссылки: {}", e.getMessage());
            throw new RuntimeException("Error generating message link", e);
        }
    }

    private void sendNotification(Long chatId, String notificationText, String messageLink) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(notificationText);
        sendMessage.setParseMode("Markdown");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> buttons = List.of(
                createInlineKeyboardButton(
                        "Посмотреть сообщение полностью",
                        null,
                        messageLink)
        );
        markup.setKeyboard(List.of(buttons));
        sendMessage.setReplyMarkup(markup);

        telegramBot.executeMessage(sendMessage);
    }
}