package ru.tbank.emailcheckerbot.service.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.emailcheckerbot.configuration.property.provider.EmailProviderProperties;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailSessionPropertiesServiceTest {

    @Mock
    private EmailProviderProperties emailProviderProperties;

    @InjectMocks
    private EmailSessionPropertiesService emailSessionPropertiesService;

    @Test
    void getSessionProperties_shouldReturnCorrectPropertiesForSupportedProvider() {
        String providerName = "gmail";
        EmailProviderProperties.ProviderConfig config = new EmailProviderProperties.ProviderConfig();
        config.setHost("imap.gmail.com");
        config.setPort("993");
        config.setSsl(true);
        config.setAuth("XOAUTH2");

        when(emailProviderProperties.getProviderConfig(providerName)).thenReturn(config);

        Properties result = emailSessionPropertiesService.getSessionProperties(providerName);

        assertEquals("imap.gmail.com", result.getProperty("mail.imap.host"));
        assertEquals("993", result.getProperty("mail.imap.port"));
        assertEquals("true", result.getProperty("mail.imap.ssl.enable"));
        assertEquals("XOAUTH2", result.getProperty("mail.imap.auth.mechanisms"));
    }

    @Test
    void getSessionProperties_shouldThrowExceptionForUnsupportedProvider() {
        String providerName = "unsupportedProvider";

        when(emailProviderProperties.getProviderConfig(providerName)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> emailSessionPropertiesService.getSessionProperties(providerName));
    }
}
