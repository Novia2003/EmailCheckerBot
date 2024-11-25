package ru.tbank.emailcheckerbot.service.provider.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.exeption.MailServiceNotFoundException;
import ru.tbank.emailcheckerbot.service.provider.MailService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MailServiceFactory {

    private final Map<MailProvider, MailService> services = new HashMap<>();

    @Autowired
    public MailServiceFactory(List<MailService> serviceList) {
        for (MailService service : serviceList) {
            services.put(service.getMailProvider(), service);
        }
    }

    public MailService getService(MailProvider provider) {
        MailService service = services.get(provider);

        if (service == null) {
            throw new MailServiceNotFoundException("Service for " + provider.getTitle() + " provider could not be found.");
        }

        return service;
    }
}
