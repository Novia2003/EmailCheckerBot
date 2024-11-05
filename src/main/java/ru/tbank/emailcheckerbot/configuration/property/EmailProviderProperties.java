package ru.tbank.emailcheckerbot.configuration.property;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties("email")
public class EmailProviderProperties {

    private Map<String, ProviderConfig> providers;

    @Data
    public static class ProviderConfig {
        private String host;
        private String port;
        private boolean ssl;
        private String auth;
    }

    public ProviderConfig getProviderConfig(String providerName) {
        return providers.getOrDefault(providerName, null);
    }
}
