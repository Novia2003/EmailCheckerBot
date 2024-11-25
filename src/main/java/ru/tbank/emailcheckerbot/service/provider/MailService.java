package ru.tbank.emailcheckerbot.service.provider;

import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;

public interface MailService {

    String getAuthUrl(Long userId);

    String getEmail(String token);

    String getSettingsUrl();

    AccessTokenResponseDTO getAccessTokenResponse(String code);

    RefreshTokenResponseDTO getRefreshTokenResponse(String refreshToken);

    MailProvider getMailProvider();

    String getInstructionForPermission();
}
