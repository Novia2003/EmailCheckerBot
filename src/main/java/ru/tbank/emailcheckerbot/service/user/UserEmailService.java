package ru.tbank.emailcheckerbot.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;

@Service
@RequiredArgsConstructor
public class UserEmailService {

    private final UserEmailRedisService userEmailRedisService;
    private final UserEmailJpaService userEmailJpaService;

    public MailProvider getMailProvider(Long id, boolean isEmailRegistered) {
        return (isEmailRegistered) ?
                userEmailJpaService.getMailProvider(id) : userEmailRedisService.getMailProvider(id);
    }

    public String getRefreshToken(Long id, boolean isEmailRegistered) {
        return (isEmailRegistered) ?
                userEmailJpaService.getRefreshToken(id) : userEmailRedisService.getRefreshToken(id);
    }

    public void saveRefreshTokenResponse(Long id, boolean isEmailRegistered, RefreshTokenResponseDTO refreshTokenResponseDTO) {
        if (isEmailRegistered) {
                userEmailJpaService.saveRefreshTokenResponse(id, refreshTokenResponseDTO);
        } else {
            userEmailRedisService.saveRefreshTokenResponse(id, refreshTokenResponseDTO);
        }
    }
}
