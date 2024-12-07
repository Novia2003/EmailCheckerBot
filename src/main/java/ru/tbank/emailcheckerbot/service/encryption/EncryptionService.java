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
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final EncryptionProperties encryptionProperties;

    public String encodeId(long id) {
        try {
            String idStr = String.valueOf(id);
            byte[] idBytes = idStr.getBytes();

            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
            byte[] encryptedBytes = cipher.doFinal(idBytes);

            return BASE64_URL_ENCODER.encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Error encrypting id", e);
            throw new EncryptionException("Error encrypting id", e);
        }
    }

    public long decodeId(String encodedStr) {
        try {
            byte[] encryptedBytes = BASE64_URL_DECODER.decode(encodedStr);

            Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            String idStr = new String(decryptedBytes);

            return Long.parseLong(idStr);
        } catch (Exception e) {
            log.error("Error decrypting the string", e);
            throw new DecryptionException("Error decrypting the string", e);
        }
    }

    public byte[] encryptToken(String token) {
        try {
            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
            return cipher.doFinal(token.getBytes());
        } catch (Exception e) {
            throw new EncryptionException("Error encrypting token", e);
        }
    }

    public String decryptToken(byte[] dbData) {
        try {
            Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
            return new String(cipher.doFinal(dbData));
        } catch (Exception e) {
            throw new DecryptionException("Error decrypting token", e);
        }
    }

    private SecretKeySpec getSecretKey() {
        byte[] decodedKey = BASE64_DECODER.decode(encryptionProperties.getKey());
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }

    private Cipher getCipher(int mode) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(mode, getSecretKey());
        return cipher;
    }
}
