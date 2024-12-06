package ru.tbank.emailcheckerbot.service.encryption;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tbank.emailcheckerbot.configuration.property.EncryptionProperties;
import ru.tbank.emailcheckerbot.exeption.DecryptionException;
import ru.tbank.emailcheckerbot.exeption.EncryptionException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class EncryptionService {

    private static final String ALGORITHM = "AES";

    private final EncryptionProperties encryptionProperties;

    public String encodeId(long id) {
        try {
            String idStr = String.valueOf(id);
            byte[] idBytes = idStr.getBytes();

            byte[] decodedKey = Base64.getDecoder().decode(encryptionProperties.getKey());
            SecretKeySpec secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(idBytes);

            return Base64.getUrlEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Error encrypting id", e);
            throw new EncryptionException("Error encrypting id", e);
        }
    }

    public long decodeId(String encodedStr) {
        try {

            byte[] encryptedBytes = Base64.getUrlDecoder().decode(encodedStr);

            byte[] decodedKey = Base64.getDecoder().decode(encryptionProperties.getKey());
            SecretKeySpec secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            String idStr = new String(decryptedBytes);

            return Long.parseLong(idStr);
        } catch (Exception e) {
            log.error("Error decrypting the string", e);
            throw new DecryptionException("Error decrypting the string", e);
        }
    }
}