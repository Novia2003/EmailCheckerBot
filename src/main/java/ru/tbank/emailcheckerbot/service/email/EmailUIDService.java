package ru.tbank.emailcheckerbot.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.entity.jpa.UserEmailJpaEntity;
import ru.tbank.emailcheckerbot.dto.message.EmailMessageDTO;
import ru.tbank.emailcheckerbot.exeption.DecryptionException;
import ru.tbank.emailcheckerbot.exeption.MessageNotFoundException;
import ru.tbank.emailcheckerbot.exeption.UserEmailJpaEntityNotFoundException;
import ru.tbank.emailcheckerbot.service.authentication.AuthenticationService;
import ru.tbank.emailcheckerbot.service.encryption.EncryptionService;
import ru.tbank.emailcheckerbot.service.user.UserEmailJpaService;

import java.time.Instant;
import java.util.Properties;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailUIDService {

    private final EmailSessionPropertiesService emailSessionPropertiesService;
    private final EmailSessionService emailSessionService;
    private final UserEmailJpaService userEmailJpaService;
    private final AuthenticationService authenticationService;
    private final EncryptionService encryptionService;

    public long getLastMessageUID(String email, MailProvider emailProvider, String token) {
        Properties properties = emailSessionPropertiesService.getSessionProperties(emailProvider.getConfigurationName());

        return emailSessionService.getLastMessageUID(properties, email, token);
    }

    public String getMessageByUID(String encodedUserEmailId, String encodedMessageUID) {
        long userEmailId;
        long messageUID;

        try {
            userEmailId = encryptionService.decodeId(encodedUserEmailId);
            messageUID = encryptionService.decodeId(encodedMessageUID);
        } catch (DecryptionException e) {
            log.error("Error decrypting input parameters", e);
            return "Произошла ошибка при расшифровании входных параметров";
        }

        UserEmailJpaEntity userEmail;

        try {
            userEmail = userEmailJpaService.getUserEmailJpaEntity(userEmailId);
        } catch (UserEmailJpaEntityNotFoundException e) {
            log.info("Запись о почте с id {} не найдено", userEmailId);
            return "Запись о почте с encodedUserEmailId " + encodedUserEmailId + " не найдена";
        }

        Properties properties = emailSessionPropertiesService.getSessionProperties(
                userEmail.getMailProvider().getConfigurationName()
        );

        if (Instant.now().isAfter(userEmail.getEndAccessTokenLife())) {
            authenticationService.refreshToken(userEmail.getId(), true);
        }

        try {
            EmailMessageDTO message = emailSessionService.getMessageByUID(
                    properties,
                    userEmail.getEmail(),
                    userEmail.getAccessToken(),
                    messageUID
            );

            return message.getContent();
        } catch (MessageNotFoundException e) {
            log.info("Сообщение с id {} не найдено", messageUID);
            return "Сообщение с encodeMessageUID " + encodedMessageUID + "не найдено";
        }
    }
}