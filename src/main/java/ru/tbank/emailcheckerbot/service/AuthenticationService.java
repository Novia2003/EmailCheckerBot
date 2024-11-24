package ru.tbank.emailcheckerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tbank.emailcheckerbot.bot.command.Command;
import ru.tbank.emailcheckerbot.entity.MailProvider;
import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserEmailRedisService userEmailRedisService;
    private final UserEmailPostgreService userEmailPostgreService;
    private final MailRuService mailRuService;
    private final YandexService yandexService;

    public String authenticate(String state, String code) {
        String[] userIdAndProvider = state.split(" ");
        Long userId = Long.parseLong(userIdAndProvider[0]);

        System.out.println(code);

        if (userEmailRedisService.isUserEmailRecordNotExists(userId)) {
            return getUserInactivityMessage();
        }

        MailProvider provider = MailProvider.valueOf(userIdAndProvider[1]);

        AccessTokenResponseDTO accessTokenResponse;
        String email;

        switch (provider) {
            case YANDEX -> {
                accessTokenResponse = yandexService.getAccessTokenResponse(code);
                email = yandexService.getEmail(accessTokenResponse.getAccessToken());
            }

            case MAILRu -> {
                accessTokenResponse = mailRuService.getAccessToken(code);
                email = mailRuService.getEmail(accessTokenResponse.getAccessToken());
            }

            default -> throw new IllegalStateException("Unexpected value: " + provider);
        }

        userEmailRedisService.saveAccessTokenResponseAndEmail(
                userId,
                accessTokenResponse,
                provider,
                email
                );

        return getSuccessfulGettingTokenMessage(email);
    }

    private String getUserInactivityMessage() {
        return  "Вы долго бездействовали, и запись о Вас была удалена.\n" +
                "Пожалуйста, начните процесс добавления почты с начала, набрав команду " + Command.ADD_EMAIL.getTitle();
    }

    private String getSuccessfulGettingTokenMessage(String email) {
        return "Токен для работы с почтой " + email + " успешно получен.\n" +
                "Можете вернуться в чат и смело нажать кнопку \"Выполнено\"";
    }

    public void refreshToken(Long id, boolean isEmailRegistered) {
        MailProvider provider = (isEmailRegistered) ?
                userEmailPostgreService.getMailProvider(id) : userEmailRedisService.getMailProvider(id);

        RefreshTokenResponseDTO refreshTokenResponseDTO;

        switch (provider) {
            case YANDEX -> refreshTokenResponseDTO = yandexService.getRefreshTokenResponse(
                    (isEmailRegistered) ?
                            userEmailPostgreService.getRefreshToken(id) : userEmailRedisService.getRefreshToken(id)
            );

            case MAILRu -> refreshTokenResponseDTO = mailRuService.getRefreshTokenResponse(
                    (isEmailRegistered) ?
                            userEmailPostgreService.getRefreshToken(id) : userEmailRedisService.getRefreshToken(id)
            );

            default -> throw new IllegalStateException("Unexpected value: " + provider);
        }

        if (isEmailRegistered) {
            userEmailPostgreService.saveRefreshTokenResponse(id, refreshTokenResponseDTO);
        } else {
            userEmailRedisService.saveRefreshTokenResponse(id, refreshTokenResponseDTO);
        }
    }
}
