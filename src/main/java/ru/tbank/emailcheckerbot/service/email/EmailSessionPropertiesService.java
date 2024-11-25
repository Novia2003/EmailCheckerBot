package ru.tbank.emailcheckerbot.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tbank.emailcheckerbot.configuration.property.provider.EmailProviderProperties;

import java.util.Properties;

@Service
@RequiredArgsConstructor
public class EmailSessionPropertiesService {

    private final EmailProviderProperties emailProviderProperties;

    public Properties getSessionProperties(String providerName) {
        EmailProviderProperties.ProviderConfig config = emailProviderProperties.getProviderConfig(providerName);

        if (config == null) {
            throw new IllegalArgumentException("Unsupported email provider: " + providerName);
        }

        Properties props = new Properties();
        props.put("mail.imap.host", config.getHost());
        props.put("mail.imap.port", config.getPort());
        props.put("mail.imap.ssl.enable", String.valueOf(config.isSsl()));
        props.put("mail.imap.auth.mechanisms", config.getAuth());

        return props;
    }
}
