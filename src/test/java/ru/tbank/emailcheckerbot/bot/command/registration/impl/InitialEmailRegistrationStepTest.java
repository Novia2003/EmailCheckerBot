package ru.tbank.emailcheckerbot.bot.command.registration.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.tbank.emailcheckerbot.bot.command.registration.RegistrationStep;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.service.user.UserEmailRedisService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitialEmailRegistrationStepTest {

    @Mock
    private UserEmailRedisService userEmailRedisService;

    @InjectMocks
    private InitialEmailRegistrationStep initialEmailRegistrationStep;

    @Test
    void execute_shouldCreateUserEmailRedisEntityAndReturnMessageWithProviderSelection() {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(12345L);
        User user = new User();
        user.setId(67890L);
        message.setChat(chat);
        message.setFrom(user);
        update.setMessage(message);

        doNothing().when(userEmailRedisService).createUserEmailRedisEntity(user.getId(), chat.getId());

        SendMessage result = initialEmailRegistrationStep.execute(update);

        assertEquals(Long.toString(chat.getId()), result.getChatId());
        assertEquals("Пожалуйста, выберите почтового провайдера:", result.getText());

        InlineKeyboardMarkup markup = (InlineKeyboardMarkup) result.getReplyMarkup();
        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();
        assertEquals(1, keyboard.size());
        assertEquals(MailProvider.values().length, keyboard.get(0).size());

        for (int i = 0; i < MailProvider.values().length; i++) {
            InlineKeyboardButton button = keyboard.get(0).get(i);
            assertEquals(MailProvider.values()[i].getTitle(), button.getText());
            assertEquals("/add_email " + RegistrationStep.CHOOSING_PROVIDER + " " + MailProvider.values()[i], button.getCallbackData());
        }

        verify(userEmailRedisService).createUserEmailRedisEntity(user.getId(), chat.getId());
    }

    @Test
    void getRegistrationStep_shouldReturnInitialStep() {
        RegistrationStep result = initialEmailRegistrationStep.getRegistrationStep();

        assertEquals(RegistrationStep.INITIAL, result);
    }
}
