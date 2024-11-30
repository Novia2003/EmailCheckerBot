package ru.tbank.emailcheckerbot.service.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.entity.redis.UserEmailRedisEntity;
import ru.tbank.emailcheckerbot.exeption.UserEmailRedisEntityNotFoundException;
import ru.tbank.emailcheckerbot.repository.redis.UserEmailRedisRepository;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEmailRedisServiceTest {

    @Mock
    private UserEmailRedisRepository userEmailRedisRepository;

    @Mock
    private UserEmailJpaService userEmailJpaService;

    @InjectMocks
    private UserEmailRedisService userEmailRedisService;

    @Test
    void createUserEmailRedisEntity_shouldSaveNewEntity() {
        Long userId = 1L;
        Long chatId = 12345L;

        userEmailRedisService.createUserEmailRedisEntity(userId, chatId);

        ArgumentCaptor<UserEmailRedisEntity> captor = ArgumentCaptor.forClass(UserEmailRedisEntity.class);
        verify(userEmailRedisRepository).save(captor.capture());

        UserEmailRedisEntity capturedEntity = captor.getValue();
        assertEquals(userId, capturedEntity.getUserId());
        assertEquals(chatId, capturedEntity.getChatId());
    }

    @Test
    void saveAccessTokenResponseAndEmail_shouldUpdateEntity() {
        Long userId = 1L;
        AccessTokenResponseDTO dto = new AccessTokenResponseDTO();
        dto.setAccessToken("6c3c7dcebeba73ba878439237b8cdc302031313737363830");
        dto.setRefreshToken("b42019a80b7d76b388b504cc4366e98425fcbd3e37363830");
        dto.setExpiresIn(3600L);
        MailProvider mailProvider = MailProvider.YANDEX;
        String email = "slavik@mail.ru";

        UserEmailRedisEntity userEmailRedisEntity = new UserEmailRedisEntity();
        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.of(userEmailRedisEntity));

        userEmailRedisService.saveAccessTokenResponseAndEmail(userId, dto, mailProvider, email);

        ArgumentCaptor<UserEmailRedisEntity> captor = ArgumentCaptor.forClass(UserEmailRedisEntity.class);
        verify(userEmailRedisRepository).save(captor.capture());

        UserEmailRedisEntity capturedEntity = captor.getValue();
        assertEquals("6c3c7dcebeba73ba878439237b8cdc302031313737363830", capturedEntity.getAccessToken());
        assertEquals("b42019a80b7d76b388b504cc4366e98425fcbd3e37363830", capturedEntity.getRefreshToken());
        assertEquals(mailProvider, capturedEntity.getMailProvider());
        assertEquals(email, capturedEntity.getEmail());
        assertNotNull(capturedEntity.getEndAccessTokenLife());
    }

    @Test
    void saveRefreshTokenResponse_shouldUpdateEntity() {
        Long userId = 1L;
        RefreshTokenResponseDTO dto = new RefreshTokenResponseDTO();
        dto.setAccessToken("6c3c7dcebeba73ba878439237b8cdc302031313737363830");
        dto.setRefreshToken("b42019a80b7d76b388b504cc4366e98425fcbd3e37363830");
        dto.setExpiresIn(3600L);

        UserEmailRedisEntity userEmailRedisEntity = new UserEmailRedisEntity();
        userEmailRedisEntity.setMailProvider(MailProvider.YANDEX);

        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.of(userEmailRedisEntity));

        userEmailRedisService.saveRefreshTokenResponse(userId, dto);

        ArgumentCaptor<UserEmailRedisEntity> captor = ArgumentCaptor.forClass(UserEmailRedisEntity.class);
        verify(userEmailRedisRepository).save(captor.capture());

        UserEmailRedisEntity capturedEntity = captor.getValue();
        assertEquals("6c3c7dcebeba73ba878439237b8cdc302031313737363830", capturedEntity.getAccessToken());
        assertEquals("b42019a80b7d76b388b504cc4366e98425fcbd3e37363830", capturedEntity.getRefreshToken());
        assertNotNull(capturedEntity.getEndAccessTokenLife());
    }

    @Test
    void isUserEmailRecordNotExists_shouldReturnTrueWhenEntityNotFound() {
        Long userId = 1L;

        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.empty());

        boolean result = userEmailRedisService.isUserEmailRecordNotExists(userId);

        assertTrue(result);
    }

    @Test
    void isUserEmailRecordNotExists_shouldReturnFalseWhenEntityExists() {
        Long userId = 1L;
        UserEmailRedisEntity userEmailRedisEntity = new UserEmailRedisEntity();

        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.of(userEmailRedisEntity));

        boolean result = userEmailRedisService.isUserEmailRecordNotExists(userId);

        assertFalse(result);
    }

    @Test
    void getAccessToken_shouldReturnAccessToken() {
        Long userId = 1L;
        UserEmailRedisEntity userEmailRedisEntity = new UserEmailRedisEntity();
        userEmailRedisEntity.setAccessToken("6c3c7dcebeba73ba878439237b8cdc302031313737363830");

        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.of(userEmailRedisEntity));

        String result = userEmailRedisService.getAccessToken(userId);

        assertEquals("6c3c7dcebeba73ba878439237b8cdc302031313737363830", result);
    }

    @Test
    void getAccessToken_shouldThrowExceptionWhenEntityNotFound() {
        Long userId = 1L;

        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserEmailRedisEntityNotFoundException.class, () -> userEmailRedisService.getAccessToken(userId));
    }

    @Test
    void getRefreshToken_shouldReturnRefreshToken() {
        Long userId = 1L;
        UserEmailRedisEntity userEmailRedisEntity = new UserEmailRedisEntity();
        userEmailRedisEntity.setRefreshToken("b42019a80b7d76b388b504cc4366e98425fcbd3e37363830");

        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.of(userEmailRedisEntity));

        String result = userEmailRedisService.getRefreshToken(userId);

        assertEquals("b42019a80b7d76b388b504cc4366e98425fcbd3e37363830", result);
    }

    @Test
    void getRefreshToken_shouldThrowExceptionWhenEntityNotFound() {
        Long userId = 1L;

        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserEmailRedisEntityNotFoundException.class, () -> userEmailRedisService.getRefreshToken(userId));
    }

    @Test
    void getMailProvider_shouldReturnMailProvider() {
        Long userId = 1L;
        UserEmailRedisEntity userEmailRedisEntity = new UserEmailRedisEntity();
        userEmailRedisEntity.setMailProvider(MailProvider.YANDEX);

        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.of(userEmailRedisEntity));

        MailProvider result = userEmailRedisService.getMailProvider(userId);

        assertEquals(MailProvider.YANDEX, result);
    }

    @Test
    void getMailProvider_shouldThrowExceptionWhenEntityNotFound() {
        Long userId = 1L;

        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserEmailRedisEntityNotFoundException.class, () -> userEmailRedisService.getMailProvider(userId));
    }

    @Test
    void getEndAccessTokenLife_shouldReturnEndAccessTokenLife() {
        Long userId = 1L;
        UserEmailRedisEntity userEmailRedisEntity = new UserEmailRedisEntity();
        userEmailRedisEntity.setEndAccessTokenLife(Instant.now());

        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.of(userEmailRedisEntity));

        Instant result = userEmailRedisService.getEndAccessTokenLife(userId);

        assertEquals(userEmailRedisEntity.getEndAccessTokenLife(), result);
    }

    @Test
    void getEndAccessTokenLife_shouldThrowExceptionWhenEntityNotFound() {
        Long userId = 1L;

        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserEmailRedisEntityNotFoundException.class, () -> userEmailRedisService.getEndAccessTokenLife(userId));
    }

    @Test
    void setLastMessageUID_shouldUpdateLastMessageUID() {
        Long userId = 1L;
        Long lastMessageUID = 100L;
        UserEmailRedisEntity userEmailRedisEntity = new UserEmailRedisEntity();

        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.of(userEmailRedisEntity));

        userEmailRedisService.setLastMessageUID(userId, lastMessageUID);

        ArgumentCaptor<UserEmailRedisEntity> captor = ArgumentCaptor.forClass(UserEmailRedisEntity.class);
        verify(userEmailRedisRepository).save(captor.capture());

        UserEmailRedisEntity capturedEntity = captor.getValue();
        assertEquals(lastMessageUID, capturedEntity.getLastMessageUID());
    }

    @Test
    void setLastMessageUID_shouldThrowExceptionWhenEntityNotFound() {
        Long userId = 1L;
        Long lastMessageUID = 100L;

        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserEmailRedisEntityNotFoundException.class, () -> userEmailRedisService.setLastMessageUID(userId, lastMessageUID));
    }

    @Test
    void transferEntityFromRedisToJpa_shouldTransferEntityAndDeleteFromRedis() {
        Long userId = 1L;
        UserEmailRedisEntity userEmailRedisEntity = new UserEmailRedisEntity();

        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.of(userEmailRedisEntity));

        userEmailRedisService.transferEntityFromRedisToJpa(userId);

        verify(userEmailJpaService).saveEntityFromRedis(userEmailRedisEntity);
        verify(userEmailRedisRepository).delete(userEmailRedisEntity);
    }

    @Test
    void transferEntityFromRedisToJpa_shouldThrowExceptionWhenEntityNotFound() {
        Long userId = 1L;

        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserEmailRedisEntityNotFoundException.class, () -> userEmailRedisService.transferEntityFromRedisToJpa(userId));
    }

    @Test
    void deleteEntity_shouldDeleteEntity() {
        Long userId = 1L;
        UserEmailRedisEntity userEmailRedisEntity = new UserEmailRedisEntity();

        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.of(userEmailRedisEntity));

        userEmailRedisService.deleteEntity(userId);

        verify(userEmailRedisRepository).delete(userEmailRedisEntity);
    }

    @Test
    void deleteEntity_shouldThrowExceptionWhenEntityNotFound() {
        Long userId = 1L;

        when(userEmailRedisRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserEmailRedisEntityNotFoundException.class, () -> userEmailRedisService.deleteEntity(userId));
    }
}
