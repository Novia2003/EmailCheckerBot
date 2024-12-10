package ru.tbank.emailcheckerbot.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.tbank.emailcheckerbot.service.email.EmailUIDService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(MessageController.class)
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailUIDService emailUIDService;

    @Test
    public void testGetClients_shouldReturnMessageContentWhenValid() throws Exception {
        String encodedUserEmailId = "encodedUserEmailId";
        String encodedMessageUID = "encodedMessageUID";
        String expectedContent = "Все будет хорошо";

        when(emailUIDService.getMessageByUID(encodedUserEmailId, encodedMessageUID)).thenReturn(expectedContent);

        mockMvc.perform(get("/api/v1/messages")
                        .param("encodedUserEmailId", encodedUserEmailId)
                        .param("encodedMessageUID", encodedMessageUID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name("message"))
                .andExpect(content().string(containsString(expectedContent)));
    }

    @Test
    public void testGetClients_shouldReturnErrorMessageWhenDecryptionFails() throws Exception {
        String encodedUserEmailId = "encodedUserEmailId";
        String encodedMessageUID = "encodedMessageUID";
        String expectedContent = "Произошла ошибка при расшифровании входных параметров";

        when(emailUIDService.getMessageByUID(encodedUserEmailId, encodedMessageUID)).thenReturn(expectedContent);

        mockMvc.perform(get("/api/v1/messages")
                        .param("encodedUserEmailId", encodedUserEmailId)
                        .param("encodedMessageUID", encodedMessageUID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name("message"))
                .andExpect(content().string(containsString(expectedContent)));
    }

    @Test
    public void testGetClients_shouldReturnErrorMessageWhenUserEmailNotFound() throws Exception {
        String encodedUserEmailId = "encodedUserEmailId";
        String encodedMessageUID = "encodedMessageUID";
        String expectedContent = "Запись о почте с encodedUserEmailId " + encodedUserEmailId + " не найдена";

        when(emailUIDService.getMessageByUID(encodedUserEmailId, encodedMessageUID)).thenReturn(expectedContent);

        mockMvc.perform(get("/api/v1/messages")
                        .param("encodedUserEmailId", encodedUserEmailId)
                        .param("encodedMessageUID", encodedMessageUID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name("message"))
                .andExpect(content().string(containsString(expectedContent)));
    }

    @Test
    public void testGetClients_shouldReturnErrorMessageWhenMessageNotFound() throws Exception {
        String encodedUserEmailId = "encodedUserEmailId";
        String encodedMessageUID = "encodedMessageUID";
        String expectedContent = "Сообщение с encodeMessageUID " + encodedMessageUID + "не найдено";

        when(emailUIDService.getMessageByUID(encodedUserEmailId, encodedMessageUID)).thenReturn(expectedContent);

        mockMvc.perform(get("/api/v1/messages")
                        .param("encodedUserEmailId", encodedUserEmailId)
                        .param("encodedMessageUID", encodedMessageUID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name("message"))
                .andExpect(content().string(containsString(expectedContent)));
    }
}
