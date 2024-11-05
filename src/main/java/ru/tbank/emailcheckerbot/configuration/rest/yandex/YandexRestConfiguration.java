package ru.tbank.emailcheckerbot.configuration.rest.yandex;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.tbank.emailcheckerbot.configuration.property.YandexRestProperties;

@Configuration
@RequiredArgsConstructor
public class YandexRestConfiguration {

    @Bean
    public RestTemplate yandexRestTemplate(
            RestTemplateBuilder restTemplateBuilder,
            YandexRestProperties properties
    ) {
        return restTemplateBuilder
                .rootUri(properties.getUrl())
                .setConnectTimeout(properties.getConnectTimeout())
                .setReadTimeout(properties.getReadTimeout())
                .build();
    }
}