package ru.tbank.emailcheckerbot.bot.command.registration.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.emailcheckerbot.bot.command.registration.EmailRegistrationStep;
import ru.tbank.emailcheckerbot.bot.command.registration.RegistrationStep;
import ru.tbank.emailcheckerbot.bot.command.registration.impl.ChoosingProviderStep;
import ru.tbank.emailcheckerbot.bot.command.registration.impl.InitialEmailRegistrationStep;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationStepFactoryTest {

    @Mock
    private InitialEmailRegistrationStep initialEmailRegistrationStep;

    @Mock
    private ChoosingProviderStep choosingProviderStep;

    @InjectMocks
    private RegistrationStepFactory registrationStepFactory;

    @Spy
    private List<EmailRegistrationStep> list = new ArrayList<>();

    @BeforeEach
    public void setup() {
        list.add(initialEmailRegistrationStep);
        list.add(choosingProviderStep);
    }

    @Test
    void getEmailRegistrationStep_shouldReturnCorrectStep() {
        when(initialEmailRegistrationStep.getRegistrationStep()).thenReturn(RegistrationStep.INITIAL);
        when(choosingProviderStep.getRegistrationStep()).thenReturn(RegistrationStep.CHOOSING_PROVIDER);
        registrationStepFactory = new RegistrationStepFactory(list);

        EmailRegistrationStep result = registrationStepFactory.getEmailRegistrationStep(RegistrationStep.INITIAL);

        assertEquals(initialEmailRegistrationStep, result);
    }

    @Test
    void getEmailRegistrationStep_shouldThrowExceptionWhenStepNotFound() {
        when(initialEmailRegistrationStep.getRegistrationStep()).thenReturn(RegistrationStep.INITIAL);
        when(choosingProviderStep.getRegistrationStep()).thenReturn(RegistrationStep.CHOOSING_PROVIDER);
        registrationStepFactory = new RegistrationStepFactory(list);

        assertThrows(NoSuchElementException.class, () ->
                registrationStepFactory.getEmailRegistrationStep(RegistrationStep.GETTING_TOKEN));
    }
}
