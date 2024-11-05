package ru.tbank.emailcheckerbot.configuration.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("rest.yandex")
public class YandexRestProperties {
    private String url;
    private Duration readTimeout;
    private Duration connectTimeout;
}
