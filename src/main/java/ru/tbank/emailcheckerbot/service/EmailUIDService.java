package ru.tbank.emailcheckerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tbank.emailcheckerbot.entity.UserEmailEntity;
import ru.tbank.emailcheckerbot.message.EmailMessage;
import ru.tbank.emailcheckerbot.repository.UserEmailRepository;

import java.util.Properties;

@Service
@RequiredArgsConstructor
public class EmailUIDService {

    private final EmailSessionPropertiesService emailSessionPropertiesService;
    private final EmailSessionService emailSessionService;

    private final UserEmailRepository userEmailRepository;

    public long getLastMessageUID(String email, String emailProvider, String token) {
        Properties properties = emailSessionPropertiesService.getSessionProperties(emailProvider);
        return emailSessionService.getLastMessageUID(properties, email, token);
    }

    public String getMessageByUID(long userEmailId, long messageUID) {
        UserEmailEntity userEmail = userEmailRepository.getReferenceById(userEmailId);
        Properties properties = emailSessionPropertiesService.getSessionProperties(userEmail.getEmailProvider());

        EmailMessage message = emailSessionService.getMessageByUID(
                properties,
                userEmail.getEmail(),
                userEmail.getAccessToken(),
                messageUID
        );

        return message.getContent();
    }
}
