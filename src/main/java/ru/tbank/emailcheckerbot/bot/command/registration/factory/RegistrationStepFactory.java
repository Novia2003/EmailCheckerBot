package ru.tbank.emailcheckerbot.bot.command.registration.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.tbank.emailcheckerbot.bot.command.registration.EmailRegistrationStep;
import ru.tbank.emailcheckerbot.bot.command.registration.RegistrationStep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
        EmailRegistrationStep foundedStep = steps.get(step);

        if (foundedStep == null) {
            throw new NoSuchElementException("No found action for step: " + step);
        }

        return foundedStep;
    }
}
