package ru.tbank.emailcheckerbot.service.provider.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.exeption.MailServiceNotFoundException;
import ru.tbank.emailcheckerbot.service.provider.MailService;
import ru.tbank.emailcheckerbot.service.provider.impl.MailRuService;
import ru.tbank.emailcheckerbot.service.provider.impl.YandexService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailServiceFactoryTest {

    @Mock
    private YandexService yandexService;

    @Mock
    private MailRuService mailRuService;

    @Spy
    private List<MailService> list = new ArrayList<>();

    @BeforeEach
    public void setup() {
        list.add(mailRuService);
        list.add(yandexService);
    }

    @InjectMocks
    private MailServiceFactory mailServiceFactory;

    @Test
    void getService_shouldReturnCorrectServiceForYandexProvider() {
        when(yandexService.getMailProvider()).thenReturn(MailProvider.YANDEX);
        List<MailService> serviceList = Arrays.asList(yandexService, mailRuService);
        mailServiceFactory = new MailServiceFactory(serviceList);

        MailService result = mailServiceFactory.getService(MailProvider.YANDEX);

        assertEquals(yandexService, result);
    }

    @Test
    void getService_shouldReturnCorrectServiceForMailRuProvider() {
        when(mailRuService.getMailProvider()).thenReturn(MailProvider.MAILRu);
        List<MailService> serviceList = Arrays.asList(yandexService, mailRuService);
        mailServiceFactory = new MailServiceFactory(serviceList);

        MailService result = mailServiceFactory.getService(MailProvider.MAILRu);

        assertEquals(mailRuService, result);
    }

    @Test
    void getService_shouldThrowExceptionWhenServiceNotFound() {
        MailProvider testProvider = mock(MailProvider.class);
        when(yandexService.getMailProvider()).thenReturn(MailProvider.YANDEX);
        when(mailRuService.getMailProvider()).thenReturn(MailProvider.MAILRu);
        List<MailService> serviceList = Arrays.asList(yandexService, mailRuService);
        mailServiceFactory = new MailServiceFactory(serviceList);

        assertThrows(MailServiceNotFoundException.class, () -> mailServiceFactory.getService(testProvider));
    }
}
