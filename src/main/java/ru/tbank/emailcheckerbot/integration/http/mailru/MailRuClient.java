package ru.tbank.emailcheckerbot.integration.http.mailru;

import ru.tbank.emailcheckerbot.dto.mailru.MailRuUserInfoDTO;
import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;

public interface MailRuClient {

    AccessTokenResponseDTO getAccessToken(String code);

    RefreshTokenResponseDTO refreshAccessToken(String refreshToken);

    MailRuUserInfoDTO getUserInfo(String token);
}
