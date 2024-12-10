package ru.tbank.emailcheckerbot.configuration.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tbank.emailcheckerbot.configuration.property.FixedThreadProperties;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@RequiredArgsConstructor
public class ExecutorConfiguration {

    private final FixedThreadProperties fixedThreadProperties;

    @Bean(name = "fixedThreadPool")
    public ExecutorService fixedThreadPool() {
        return Executors.newFixedThreadPool(fixedThreadProperties.getSize(), runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("FixedThreadPool-" + thread.getId());

            return thread;
        });
    }
}