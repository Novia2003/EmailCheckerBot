package ru.tbank.emailcheckerbot.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;

import static ru.tbank.emailcheckerbot.parser.EmailMessageContentParser.getTextFromMessage;

@Data
@AllArgsConstructor
public class EmailMessageDTO {
    private Long uid;
    private String from;
    private String subject;
    private String content;

    public static EmailMessageDTO of(Long uid, Message message) throws MessagingException, IOException {
        return new EmailMessageDTO(
                uid,
                ((InternetAddress) message.getFrom()[0]).getAddress(),
                message.getSubject(),
                getTextFromMessage(message)
        );
    }
}