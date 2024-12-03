package ru.tbank.emailcheckerbot.bot.command.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.emailcheckerbot.bot.command.registration.RegistrationStep;
import ru.tbank.emailcheckerbot.bot.command.registration.factory.RegistrationStepFactory;
import ru.tbank.emailcheckerbot.bot.command.registration.impl.InitialEmailRegistrationStep;
import ru.tbank.emailcheckerbot.exeption.InvalidCallbackQueryException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddEmailCommandTest {

    @Mock
    private RegistrationStepFactory registrationStepFactory;

    @Mock
    private InitialEmailRegistrationStep initialEmailRegistrationStep;

    @InjectMocks
    private AddEmailCommand addEmailCommand;

    @Test
    void execute_shouldReturnInitialStepWhenMessageUpdate() {
        Update update = new Update();
        Message message = new Message();
        update.setMessage(message);

        when(registrationStepFactory.getEmailRegistrationStep(RegistrationStep.INITIAL)).thenReturn(initialEmailRegistrationStep);
        when(initialEmailRegistrationStep.execute(update)).thenReturn(new SendMessage());

        SendMessage result = addEmailCommand.execute(update);

        assertNotNull(result);
        verify(registrationStepFactory).getEmailRegistrationStep(RegistrationStep.INITIAL);
        verify(initialEmailRegistrationStep).execute(update);
    }

    @Test
    void execute_shouldReturnCorrectStepWhenCallbackQuery() {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData("add_email INITIAL");
        update.setCallbackQuery(callbackQuery);

        when(registrationStepFactory.getEmailRegistrationStep(RegistrationStep.INITIAL)).thenReturn(initialEmailRegistrationStep);
        when(initialEmailRegistrationStep.execute(update)).thenReturn(new SendMessage());

        SendMessage result = addEmailCommand.execute(update);

        assertNotNull(result);
        verify(registrationStepFactory).getEmailRegistrationStep(RegistrationStep.INITIAL);
        verify(initialEmailRegistrationStep).execute(update);
    }

    @Test
    void execute_shouldThrowExceptionWhenInvalidCallbackQuery() {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData("add_email");
        update.setCallbackQuery(callbackQuery);

        assertThrows(InvalidCallbackQueryException.class, () -> addEmailCommand.execute(update));
    }

    @Test
    void getName_shouldReturnAddEmailCommandName() {
        String result = addEmailCommand.getName();

        assertEquals("/add_email", result);
    }

    @Test
    void getDescription_shouldReturnAddEmailCommandDescription() {
        String result = addEmailCommand.getDescription();

        assertEquals("команда для добавления нового почтового аккаунта", result);
    }
}
