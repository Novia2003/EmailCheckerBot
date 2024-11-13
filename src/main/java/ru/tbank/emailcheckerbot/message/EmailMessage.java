package ru.tbank.emailcheckerbot.message;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;

import static ru.tbank.emailcheckerbot.parser.EmailMessageContentParser.getTextFromMessage;

@Data
@AllArgsConstructor
public class EmailMessage {
    private Long uid;
    private String from;
    private String subject;
    private String content;

    public static EmailMessage of(Long uid, Message message) throws MessagingException, IOException {
        return new EmailMessage(
                uid,
                ((InternetAddress) message.getFrom()[0]).getAddress(),
                message.getSubject(),
                getTextFromMessage(message)
        );
    }
}