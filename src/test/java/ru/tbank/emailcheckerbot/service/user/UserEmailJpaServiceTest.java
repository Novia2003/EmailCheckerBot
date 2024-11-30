package ru.tbank.emailcheckerbot.service.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.entity.jpa.UserEmailJpaEntity;
import ru.tbank.emailcheckerbot.entity.jpa.UserJpaEntity;
import ru.tbank.emailcheckerbot.entity.redis.UserEmailRedisEntity;
import ru.tbank.emailcheckerbot.exeption.UserEmailJpaEntityNotFoundException;
import ru.tbank.emailcheckerbot.repository.jpa.UserEmailJpaRepository;
import ru.tbank.emailcheckerbot.repository.jpa.UserJpaRepository;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEmailJpaServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private UserEmailJpaRepository userEmailJpaRepository;

    @InjectMocks
    private UserEmailJpaService userEmailJpaService;

    @Test
    void saveRefreshTokenResponse_shouldUpdateUserEmailJpaEntity() {
        Long id = 1L;
        RefreshTokenResponseDTO dto = new RefreshTokenResponseDTO();
        dto.setAccessToken("6c3c7dcebeba73ba878439237b8cdc302031313737363830");
        dto.setRefreshToken("b42019a80b7d76b388b504cc4366e98425fcbd3e37363830");
        dto.setExpiresIn(3600L);

        UserEmailJpaEntity userEmailJpaEntity = new UserEmailJpaEntity();
        userEmailJpaEntity.setMailProvider(MailProvider.YANDEX);

        when(userEmailJpaRepository.findById(id)).thenReturn(Optional.of(userEmailJpaEntity));

        userEmailJpaService.saveRefreshTokenResponse(id, dto);

        ArgumentCaptor<UserEmailJpaEntity> captor = ArgumentCaptor.forClass(UserEmailJpaEntity.class);
        verify(userEmailJpaRepository).save(captor.capture());

        UserEmailJpaEntity capturedEntity = captor.getValue();
        assertEquals("6c3c7dcebeba73ba878439237b8cdc302031313737363830", capturedEntity.getAccessToken());
        assertEquals("b42019a80b7d76b388b504cc4366e98425fcbd3e37363830", capturedEntity.getRefreshToken());
        assertNotNull(capturedEntity.getEndAccessTokenLife());
    }

    @Test
    void getRefreshToken_shouldReturnRefreshToken() {
        Long id = 1L;
        UserEmailJpaEntity userEmailJpaEntity = new UserEmailJpaEntity();
        userEmailJpaEntity.setRefreshToken("b42019a80b7d76b388b504cc4366e98425fcbd3e37363830");

        when(userEmailJpaRepository.findById(id)).thenReturn(Optional.of(userEmailJpaEntity));

        String result = userEmailJpaService.getRefreshToken(id);

        assertEquals("b42019a80b7d76b388b504cc4366e98425fcbd3e37363830", result);
    }

    @Test
    void getRefreshToken_shouldThrowExceptionWhenEntityNotFound() {
        Long id = 1L;

        when(userEmailJpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserEmailJpaEntityNotFoundException.class, () -> userEmailJpaService.getRefreshToken(id));
    }

    @Test
    void getMailProvider_shouldReturnMailProvider() {
        Long id = 1L;
        UserEmailJpaEntity userEmailJpaEntity = new UserEmailJpaEntity();
        userEmailJpaEntity.setMailProvider(MailProvider.YANDEX);

        when(userEmailJpaRepository.findById(id)).thenReturn(Optional.of(userEmailJpaEntity));

        MailProvider result = userEmailJpaService.getMailProvider(id);

        assertEquals(MailProvider.YANDEX, result);
    }

    @Test
    void getMailProvider_shouldThrowExceptionWhenEntityNotFound() {
        Long id = 1L;

        when(userEmailJpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserEmailJpaEntityNotFoundException.class, () -> userEmailJpaService.getMailProvider(id));
    }

    @Test
    void saveEntityFromRedis_shouldSaveNewUserAndUserEmail() {
        UserEmailRedisEntity redisEntity = new UserEmailRedisEntity();
        redisEntity.setUserId(1L);
        redisEntity.setChatId(12345L);
        redisEntity.setMailProvider(MailProvider.YANDEX);
        redisEntity.setEmail("slavik@mail.ru");
        redisEntity.setAccessToken("6c3c7dcebeba73ba878439237b8cdc302031313737363830");
        redisEntity.setRefreshToken("b42019a80b7d76b388b504cc4366e98425fcbd3e37363830");
        redisEntity.setEndAccessTokenLife(Instant.now());
        redisEntity.setLastMessageUID(100L);

        when(userJpaRepository.existsByTelegramId(1L)).thenReturn(false);

        userEmailJpaService.saveEntityFromRedis(redisEntity);

        ArgumentCaptor<UserJpaEntity> userCaptor = ArgumentCaptor.forClass(UserJpaEntity.class);
        verify(userJpaRepository).save(userCaptor.capture());

        UserJpaEntity capturedUser = userCaptor.getValue();
        assertEquals(1L, capturedUser.getTelegramId());
        assertEquals(12345L, capturedUser.getChatId());

        ArgumentCaptor<UserEmailJpaEntity> emailCaptor = ArgumentCaptor.forClass(UserEmailJpaEntity.class);
        verify(userEmailJpaRepository).save(emailCaptor.capture());

        UserEmailJpaEntity capturedEmail = emailCaptor.getValue();
        assertEquals(MailProvider.YANDEX, capturedEmail.getMailProvider());
        assertEquals("slavik@mail.ru", capturedEmail.getEmail());
        assertEquals("6c3c7dcebeba73ba878439237b8cdc302031313737363830", capturedEmail.getAccessToken());
        assertEquals("b42019a80b7d76b388b504cc4366e98425fcbd3e37363830", capturedEmail.getRefreshToken());
        assertEquals(100L, capturedEmail.getLastMessageUID());
    }

    @Test
    void saveEntityFromRedis_shouldSaveExistingUserAndUserEmail() {
        UserEmailRedisEntity redisEntity = new UserEmailRedisEntity();
        redisEntity.setUserId(1L);
        redisEntity.setChatId(12345L);
        redisEntity.setMailProvider(MailProvider.YANDEX);
        redisEntity.setEmail("slavik@mail.ru");
        redisEntity.setAccessToken("6c3c7dcebeba73ba878439237b8cdc302031313737363830");
        redisEntity.setRefreshToken("b42019a80b7d76b388b504cc4366e98425fcbd3e37363830");
        redisEntity.setEndAccessTokenLife(Instant.now());
        redisEntity.setLastMessageUID(100L);

        UserJpaEntity existingUser = new UserJpaEntity();
        existingUser.setTelegramId(1L);
        existingUser.setChatId(12345L);

        when(userJpaRepository.existsByTelegramId(1L)).thenReturn(true);
        when(userJpaRepository.getByTelegramId(1L)).thenReturn(existingUser);

        userEmailJpaService.saveEntityFromRedis(redisEntity);

        ArgumentCaptor<UserEmailJpaEntity> emailCaptor = ArgumentCaptor.forClass(UserEmailJpaEntity.class);
        verify(userEmailJpaRepository).save(emailCaptor.capture());

        UserEmailJpaEntity capturedEmail = emailCaptor.getValue();
        assertEquals(MailProvider.YANDEX, capturedEmail.getMailProvider());
        assertEquals("slavik@mail.ru", capturedEmail.getEmail());
        assertEquals("6c3c7dcebeba73ba878439237b8cdc302031313737363830", capturedEmail.getAccessToken());
        assertEquals("b42019a80b7d76b388b504cc4366e98425fcbd3e37363830", capturedEmail.getRefreshToken());
        assertEquals(100L, capturedEmail.getLastMessageUID());
    }

    @Test
    void getUserEmails_shouldReturnAllUserEmails() {
        UserEmailJpaEntity userEmailJpaEntity = new UserEmailJpaEntity();
        when(userEmailJpaRepository.findAll()).thenReturn(Collections.singletonList(userEmailJpaEntity));

        List<UserEmailJpaEntity> result = userEmailJpaService.getUserEmails();

        assertEquals(1, result.size());
        assertEquals(userEmailJpaEntity, result.get(0));
    }

    @Test
    void setLastMessageUID_shouldUpdateLastMessageUID() {
        Long id = 1L;
        Long lastMessageUID = 100L;
        UserEmailJpaEntity userEmailJpaEntity = new UserEmailJpaEntity();

        when(userEmailJpaRepository.findById(id)).thenReturn(Optional.of(userEmailJpaEntity));

        userEmailJpaService.setLastMessageUID(id, lastMessageUID);

        ArgumentCaptor<UserEmailJpaEntity> captor = ArgumentCaptor.forClass(UserEmailJpaEntity.class);
        verify(userEmailJpaRepository).save(captor.capture());

        UserEmailJpaEntity capturedEntity = captor.getValue();
        assertEquals(lastMessageUID, capturedEntity.getLastMessageUID());
    }

    @Test
    void setLastMessageUID_shouldThrowExceptionWhenEntityNotFound() {
        Long id = 1L;
        Long lastMessageUID = 100L;

        when(userEmailJpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserEmailJpaEntityNotFoundException.class, () -> userEmailJpaService.setLastMessageUID(id, lastMessageUID));
    }

    @Test
    void getUserEmail_shouldReturnUserEmail() {
        Long id = 1L;
        UserEmailJpaEntity userEmailJpaEntity = new UserEmailJpaEntity();

        when(userEmailJpaRepository.getReferenceById(id)).thenReturn(userEmailJpaEntity);

        UserEmailJpaEntity result = userEmailJpaService.getUserEmail(id);

        assertEquals(userEmailJpaEntity, result);
    }

    @Test
    void existsByUserIdAndEmail_shouldReturnTrueWhenUserAndEmailExist() {
        Long userId = 1L;
        String email = "slavik@mail.ru";
        UserJpaEntity user = new UserJpaEntity();

        when(userJpaRepository.existsByTelegramId(userId)).thenReturn(true);
        when(userJpaRepository.getByTelegramId(userId)).thenReturn(user);
        when(userEmailJpaRepository.existsByUserAndEmail(user, email)).thenReturn(true);

        boolean result = userEmailJpaService.existsByUserIdAndEmail(userId, email);

        assertTrue(result);
    }

    @Test
    void existsByUserIdAndEmail_shouldReturnFalseWhenUserDoesNotExist() {
        Long userId = 1L;
        String email = "slavik@mail.ru";

        when(userJpaRepository.existsByTelegramId(userId)).thenReturn(false);

        boolean result = userEmailJpaService.existsByUserIdAndEmail(userId, email);

        assertFalse(result);
    }

    @Test
    void existsByUserIdAndEmail_shouldReturnFalseWhenEmailDoesNotExist() {
        Long userId = 1L;
        String email = "slavik@mail.ru";
        UserJpaEntity user = new UserJpaEntity();

        when(userJpaRepository.existsByTelegramId(userId)).thenReturn(true);
        when(userJpaRepository.getByTelegramId(userId)).thenReturn(user);
        when(userEmailJpaRepository.existsByUserAndEmail(user, email)).thenReturn(false);

        boolean result = userEmailJpaService.existsByUserIdAndEmail(userId, email);

        assertFalse(result);
    }
}
