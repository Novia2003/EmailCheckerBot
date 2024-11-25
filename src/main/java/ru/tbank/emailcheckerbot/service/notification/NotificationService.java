package ru.tbank.emailcheckerbot.service.notification;

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

import javax.mail.MessagingException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static ru.tbank.emailcheckerbot.bot.util.TelegramUtils.createInlineKeyboardButton;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final MessagesProperties messagesProperties;

    private final TelegramBot telegramBot;

    public void notifyUser(EmailMessageDTO[] newMessages, UserEmailJpaEntity userEmailJpaEntity) {
        Arrays.stream(newMessages).forEach(message -> {
            try {
                String notificationText = formatNotificationMessage(
                        message,
                        userEmailJpaEntity.getEmail()
                );

                String messageLink = getMessageLink(message.getUid(), userEmailJpaEntity.getId());
                sendNotification(userEmailJpaEntity.getUser().getChatId(), notificationText, messageLink);
            } catch (MessagingException e) {
                log.error("Ошибка при формировании уведомления: {}", e.getMessage());
            }
        });
    }

    private String formatNotificationMessage(EmailMessageDTO message, String userEmail) throws MessagingException {
        String senderEmail = message.getFrom();
        String subject = message.getSubject();
        String snippet =  extractSnippetFromMessage(message);

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
            String text = Jsoup.parse(message.getContent()).text();

            return text.length() > 100 ? text.substring(0, 100) + "..." : text;
    }

    private String getMessageLink(Long uid, Long userEmailEntityId) {
        String userEmailId = "userEmailId=" + userEmailEntityId;
        String messageUID = "messageUID=" + uid;

        return messagesProperties.getUrl() + "?" + userEmailId + "&" + messageUID;
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