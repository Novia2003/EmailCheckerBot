package ru.tbank.emailcheckerbot.repository.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.tbank.emailcheckerbot.service.encryption.EncryptionService;

@Component
@Converter
@RequiredArgsConstructor
public class TokenConverter implements AttributeConverter<String, byte[]> {

    private final EncryptionService encryptionService;

    @Override
    public byte[] convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }

        return encryptionService.encryptToken(attribute);
    }

    @Override
    public String convertToEntityAttribute(byte[] dbData) {
        if (dbData == null) {
            return null;
        }

        return encryptionService.decryptToken(dbData);
    }
}