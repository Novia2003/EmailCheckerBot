package ru.tbank.emailcheckerbot.bot.command.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RegistrationStepFactory {

    private final Map<RegistrationStep, EmailRegistrationStep> steps = new HashMap<>();

    @Autowired
    public RegistrationStepFactory(List<EmailRegistrationStep> steps) {
        for (EmailRegistrationStep step : steps) {
            this.steps.put(step.getRegistrationStep(), step);
        }
    }

    public EmailRegistrationStep getEmailRegistrationStep(RegistrationStep step) {
        return steps.getOrDefault(step, null);
    }
}
