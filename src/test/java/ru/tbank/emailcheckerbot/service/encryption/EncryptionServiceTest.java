package ru.tbank.emailcheckerbot.service.encryption;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.emailcheckerbot.configuration.property.EncryptionProperties;
import ru.tbank.emailcheckerbot.exeption.DecryptionException;
import ru.tbank.emailcheckerbot.exeption.EncryptionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EncryptionServiceTest {

    @Mock
    private EncryptionProperties encryptionProperties;

    @InjectMocks
    private EncryptionService encryptionService;

    @Test
    void encodeId_shouldReturnEncodedId() {
        long id = 12345L;
        String key = "2DdlRNggPa7f56G9r41ZlQ==";
        when(encryptionProperties.getKey()).thenReturn(key);

        String encodedId = encryptionService.encodeId(id);

        assertNotNull(encodedId);
        assertNotEquals(Long.toString(id), encodedId);
    }

    @Test
    void encodeId_shouldThrowExceptionWhenEncodingFails() {
        long id = 12345L;
        String key = "invalidKey";
        when(encryptionProperties.getKey()).thenReturn(key);

        assertThrows(EncryptionException.class, () -> encryptionService.encodeId(id));
    }

    @Test
    void decodeId_shouldReturnDecodedId() {
        String encodedStr = "9lSVnjzDFfURu7U7X9-Dyg==";
        String key = "hw0LJINnqtX/DnXSjE6WDg==";
        when(encryptionProperties.getKey()).thenReturn(key);

        Long decodedId = encryptionService.decodeId(encodedStr);

        assertNotNull(decodedId);
    }

    @Test
    void decodeId_shouldThrowExceptionWhenDecodingFails() {
        String encodedStr = "invalidEncodedString";
        String key = "invalidKey";
        when(encryptionProperties.getKey()).thenReturn(key);

        assertThrows(DecryptionException.class, () -> encryptionService.decodeId(encodedStr));
    }
}
