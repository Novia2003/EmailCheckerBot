package ru.tbank.emailcheckerbot.configuration.property.provider;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("email.providers.mailru")
public class MailRuProperties {
    private String authUrl;
    private String clientId;
    private String clientSecret;
    private String settingsUrl;
}
