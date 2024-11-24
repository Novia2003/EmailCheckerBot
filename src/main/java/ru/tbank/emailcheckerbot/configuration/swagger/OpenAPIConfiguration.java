package ru.tbank.emailcheckerbot.configuration.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "EmailCheckerBot API", version = "1.0")
)
public class OpenAPIConfiguration {
}
