package ru.tbank.emailcheckerbot.service.authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tbank.emailcheckerbot.bot.command.Command;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.service.provider.factory.MailServiceFactory;
import ru.tbank.emailcheckerbot.service.user.UserEmailRedisService;
import ru.tbank.emailcheckerbot.service.user.UserEmailService;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserEmailService userEmailService;
    private final UserEmailRedisService userEmailRedisService;

    private final MailServiceFactory mailServiceFactory;

    public String authenticate(String state, String code) {
        String[] userIdAndProvider = state.split(" ");
        Long userId = Long.parseLong(userIdAndProvider[0]);


        if (userEmailRedisService.isUserEmailRecordNotExists(userId)) {
            return getUserInactivityMessage();
        }

        MailProvider provider = MailProvider.valueOf(userIdAndProvider[1]);

        AccessTokenResponseDTO accessTokenResponse = mailServiceFactory.getService(provider).getAccessTokenResponse(code);
        String email = mailServiceFactory.getService(provider).getEmail(accessTokenResponse.getAccessToken());

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
        MailProvider provider = userEmailService.getMailProvider(id, isEmailRegistered);

        RefreshTokenResponseDTO refreshTokenResponseDTO = mailServiceFactory.getService(provider).getRefreshTokenResponse(
                userEmailService.getRefreshToken(id, isEmailRegistered)
        );

        userEmailService.saveRefreshTokenResponse(id, isEmailRegistered, refreshTokenResponseDTO);
    }
}
