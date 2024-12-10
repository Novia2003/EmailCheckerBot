package ru.tbank.emailcheckerbot.service.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.exeption.UserEmailJpaEntityNotFoundException;
import ru.tbank.emailcheckerbot.exeption.UserEmailRedisEntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEmailServiceTest {

    @Mock
    private UserEmailRedisService userEmailRedisService;

    @Mock
    private UserEmailJpaService userEmailJpaService;

    @InjectMocks
    private UserEmailService userEmailService;

    @Test
    void getMailProvider_shouldReturnMailProviderFromJpaServiceWhenEmailRegistered() {
        Long id = 1L;
        boolean isEmailRegistered = true;
        MailProvider expectedMailProvider = MailProvider.YANDEX;

        when(userEmailJpaService.getMailProvider(id)).thenReturn(expectedMailProvider);

        MailProvider result = userEmailService.getMailProvider(id, isEmailRegistered);

        assertEquals(expectedMailProvider, result);
        verify(userEmailJpaService).getMailProvider(id);
        verify(userEmailRedisService, never()).getMailProvider(id);
    }

    @Test
    void getMailProvider_shouldReturnMailProviderFromRedisServiceWhenEmailNotRegistered() {
        Long id = 1L;
        boolean isEmailRegistered = false;
        MailProvider expectedMailProvider = MailProvider.YANDEX;

        when(userEmailRedisService.getMailProvider(id)).thenReturn(expectedMailProvider);

        MailProvider result = userEmailService.getMailProvider(id, isEmailRegistered);

        assertEquals(expectedMailProvider, result);
        verify(userEmailRedisService).getMailProvider(id);
        verify(userEmailJpaService, never()).getMailProvider(id);
    }

    @Test
    void getMailProvider_shouldThrowExceptionWhenEntityNotFoundInJpaService() {
        Long id = 1L;
        boolean isEmailRegistered = true;

        when(userEmailJpaService.getMailProvider(id)).thenThrow(new UserEmailJpaEntityNotFoundException("Entity not found"));

        assertThrows(UserEmailJpaEntityNotFoundException.class, () -> userEmailService.getMailProvider(id, isEmailRegistered));
    }

    @Test
    void getMailProvider_shouldThrowExceptionWhenEntityNotFoundInRedisService() {
        Long id = 1L;
        boolean isEmailRegistered = false;

        when(userEmailRedisService.getMailProvider(id)).thenThrow(new UserEmailRedisEntityNotFoundException("Entity not found"));

        assertThrows(UserEmailRedisEntityNotFoundException.class, () -> userEmailService.getMailProvider(id, isEmailRegistered));
    }

    @Test
    void getRefreshToken_shouldReturnRefreshTokenFromJpaServiceWhenEmailRegistered() {
        Long id = 1L;
        boolean isEmailRegistered = true;
        String expectedRefreshToken = "b42019a80b7d76b388b504cc4366e98425fcbd3e37363830";

        when(userEmailJpaService.getRefreshToken(id)).thenReturn(expectedRefreshToken);

        String result = userEmailService.getRefreshToken(id, isEmailRegistered);

        assertEquals(expectedRefreshToken, result);
        verify(userEmailJpaService).getRefreshToken(id);
        verify(userEmailRedisService, never()).getRefreshToken(id);
    }

    @Test
    void getRefreshToken_shouldReturnRefreshTokenFromRedisServiceWhenEmailNotRegistered() {
        Long id = 1L;
        boolean isEmailRegistered = false;
        String expectedRefreshToken = "b42019a80b7d76b388b504cc4366e98425fcbd3e37363830";

        when(userEmailRedisService.getRefreshToken(id)).thenReturn(expectedRefreshToken);

        String result = userEmailService.getRefreshToken(id, isEmailRegistered);

        assertEquals(expectedRefreshToken, result);
        verify(userEmailRedisService).getRefreshToken(id);
        verify(userEmailJpaService, never()).getRefreshToken(id);
    }

    @Test
    void getRefreshToken_shouldThrowExceptionWhenEntityNotFoundInJpaService() {
        Long id = 1L;
        boolean isEmailRegistered = true;

        when(userEmailJpaService.getRefreshToken(id)).thenThrow(new UserEmailJpaEntityNotFoundException("Entity not found"));

        assertThrows(UserEmailJpaEntityNotFoundException.class, () -> userEmailService.getRefreshToken(id, isEmailRegistered));
    }

    @Test
    void getRefreshToken_shouldThrowExceptionWhenEntityNotFoundInRedisService() {
        Long id = 1L;
        boolean isEmailRegistered = false;

        when(userEmailRedisService.getRefreshToken(id)).thenThrow(new UserEmailRedisEntityNotFoundException("Entity not found"));

        assertThrows(UserEmailRedisEntityNotFoundException.class, () -> userEmailService.getRefreshToken(id, isEmailRegistered));
    }

    @Test
    void saveRefreshTokenResponse_shouldSaveRefreshTokenResponseInJpaServiceWhenEmailRegistered() {
        Long id = 1L;
        boolean isEmailRegistered = true;
        RefreshTokenResponseDTO dto = new RefreshTokenResponseDTO();
        dto.setAccessToken("6c3c7dcebeba73ba878439237b8cdc302031313737363830");
        dto.setRefreshToken("b42019a80b7d76b388b504cc4366e98425fcbd3e37363830");
        dto.setExpiresIn(3600L);

        userEmailService.saveRefreshTokenResponse(id, isEmailRegistered, dto);

        verify(userEmailJpaService).saveRefreshTokenResponse(id, dto);
        verify(userEmailRedisService, never()).saveRefreshTokenResponse(id, dto);
    }

    @Test
    void saveRefreshTokenResponse_shouldSaveRefreshTokenResponseInRedisServiceWhenEmailNotRegistered() {
        Long id = 1L;
        boolean isEmailRegistered = false;
        RefreshTokenResponseDTO dto = new RefreshTokenResponseDTO();
        dto.setAccessToken("6c3c7dcebeba73ba878439237b8cdc302031313737363830");
        dto.setRefreshToken("b42019a80b7d76b388b504cc4366e98425fcbd3e37363830");
        dto.setExpiresIn(3600L);

        userEmailService.saveRefreshTokenResponse(id, isEmailRegistered, dto);

        verify(userEmailRedisService).saveRefreshTokenResponse(id, dto);
        verify(userEmailJpaService, never()).saveRefreshTokenResponse(id, dto);
    }
}
