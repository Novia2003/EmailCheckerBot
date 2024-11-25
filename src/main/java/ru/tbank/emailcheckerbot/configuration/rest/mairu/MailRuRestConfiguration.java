package ru.tbank.emailcheckerbot.configuration.rest.mairu;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.tbank.emailcheckerbot.configuration.property.rest.MailRuRestProperties;

@Configuration
@RequiredArgsConstructor
public class MailRuRestConfiguration {

    @Bean
    public RestTemplate mailRuRestTemplate(
            RestTemplateBuilder restTemplateBuilder,
            MailRuRestProperties properties
    ) {
        return restTemplateBuilder
                .rootUri(properties.getUrl())
                .setConnectTimeout(properties.getConnectTimeout())
                .setReadTimeout(properties.getReadTimeout())
                .build();
    }
}
