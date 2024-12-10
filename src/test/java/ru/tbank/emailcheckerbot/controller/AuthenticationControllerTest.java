package ru.tbank.emailcheckerbot.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.tbank.emailcheckerbot.service.authentication.AuthenticationService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AuthenticationController.class)
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    public void testGetCode() throws Exception {
        String state = "1 YANDEX";
        String code = "authCode";
        String expectedContent = "Expected content";

        when(authenticationService.authenticate(state, code)).thenReturn(expectedContent);

        mockMvc.perform(get("/api/v1/oauth/receiver")
                        .param("state", state)
                        .param("code", code)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name("oauth"))
                .andExpect(content().string(containsString(expectedContent)));
    }

    @Test
    public void testGetCode_UserInactivity() throws Exception {
        String state = "1 YANDEX";
        String code = "authCode";
        String expectedContent = "Вы долго бездействовали, и запись о Вас была удалена.\n" +
                "Пожалуйста, начните процесс добавления почты с начала, набрав команду /add_email";

        when(authenticationService.authenticate(state, code)).thenReturn(expectedContent);

        mockMvc.perform(get("/api/v1/oauth/receiver")
                        .param("state", state)
                        .param("code", code)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name("oauth"))
                .andExpect(content().string(containsString(expectedContent)));
    }

    @Test
    public void testGetCode_EmailAlreadyRegistered() throws Exception {
        String state = "1 YANDEX";
        String code = "authCode";
        String expectedContent = "Почта slavik@mail.ru уже была зарегистрирована. Регистрация прекращена";

        when(authenticationService.authenticate(state, code)).thenReturn(expectedContent);

        mockMvc.perform(get("/api/v1/oauth/receiver")
                        .param("state", state)
                        .param("code", code)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name("oauth"))
                .andExpect(content().string(containsString(expectedContent)));
    }

    @Test
    public void testGetCode_SuccessfulGettingToken() throws Exception {
        String state = "1 YANDEX";
        String code = "authCode";
        String expectedContent = "Токен для работы с почтой slavik@mail.ru успешно получен.\n" +
                "Можете вернуться в чат и смело нажать кнопку \"Выполнено\"";

        when(authenticationService.authenticate(state, code)).thenReturn(expectedContent);

        mockMvc.perform(get("/api/v1/oauth/receiver")
                        .param("state", state)
                        .param("code", code)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name("oauth"))
                .andExpect(content().string(containsString(expectedContent.replace("\"", "&quot;"))));
    }
}
