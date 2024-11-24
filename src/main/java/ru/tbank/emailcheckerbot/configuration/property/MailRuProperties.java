package ru.tbank.emailcheckerbot.configuration.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("mailru")
public class MailRuProperties {
    private String url;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String settingsUrl;
}
