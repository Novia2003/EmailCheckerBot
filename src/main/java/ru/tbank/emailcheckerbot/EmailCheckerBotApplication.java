package ru.tbank.emailcheckerbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EmailCheckerBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailCheckerBotApplication.class, args);
	}

}
